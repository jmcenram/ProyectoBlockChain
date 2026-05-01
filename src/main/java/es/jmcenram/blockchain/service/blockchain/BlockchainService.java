package es.jmcenram.blockchain.service.blockchain;

import es.jmcenram.blockchain.config.BlockchainConfig;
import es.jmcenram.blockchain.contract.RegistroDocumentos;
import es.jmcenram.blockchain.dto.DocumentoDTO;
import es.jmcenram.blockchain.mapper.BlockchainEstadoMapper;
import lombok.Getter;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Servicio central encargado de la comunicacion con blockchain.
 *
 * Permite:
 * - Inicializar Web3j
 * - Cargar el contrato activo
 * - Registrar hashes
 * - Revocar documentos
 * - Consultar informacion del contrato
 *
 * Separa las lecturas sin firma de las escrituras firmadas con private keys de entidades emisoras.
 *
 * Forma parte de la capa de servicio e integracion blockchain.
 *
 * @author Jcena
 * @version 1.0
 */
@Getter
public class BlockchainService {

    private static BlockchainService instance;

    private final Web3j web3j;

    /**
     * Contrato en modo lectura (sin necesidad de private key).
     */
    private final RegistroDocumentos contrato;

    private final ExecutorService callbackPool = Executors.newFixedThreadPool(2);

    private final Object nonceLock = new Object();

    /**
     * Inicializa la instancia unica del servicio blockchain con la configuracion validada al arrancar.
     *
     * El resto de la aplicacion accede a esta instancia compartida para no abrir conexiones Web3j duplicadas.
     *
     * @param config configuracion blockchain desde la que se construyen las conexiones y el contrato
     */
    public static void init(BlockchainConfig config) {
        instance = new BlockchainService(config);
    }

    /**
     * Recupera la instancia blockchain ya inicializada.
     *
     * Lanza error si se pide antes del arranque para detectar configuraciones incompletas en lugar de fallar mas tarde con null.
     *
     * @return resultado calculado a partir de la operacion documentada
     */
    public static BlockchainService getInstance() {
        if (instance == null) {
            throw new RuntimeException("BlockchainService no inicializado");
        }
        return instance;
    }

    /**
     * Construye el cliente Web3j y el wrapper del contrato en modo lectura.
     *
     * La carga usa credenciales dummy y gas cero porque las consultas no firman transacciones; las escrituras crean un contrato con clave real aparte.
     *
     * @param config configuracion blockchain desde la que se construyen las conexiones y el contrato
     */
    private BlockchainService(BlockchainConfig config) {
        try {

            web3j = Web3j.build(new HttpService(config.getRpcUrl()));

            contrato = RegistroDocumentos.load(
                    config.getContractAddress(),
                    web3j,
                    new RawTransactionManager(
                            web3j,
                            Credentials.create("0x0"), // dummy
                            11155111
                    ),
                    new StaticGasProvider(
                            BigInteger.ZERO,
                            BigInteger.ZERO
                    )
            );

            System.out.println("📄 Contrato cargado: " + contrato.getContractAddress());

        } catch (Exception e) {
            throw new RuntimeException("Error inicializando BlockchainService", e);
        }
    }

    // =========================================================
    // =========================================================

    /**
     * Crea un wrapper del contrato preparado para firmar escrituras con una private key concreta.
     *
     * Se separa del contrato de lectura para que cada entidad emisora firme sus propias operaciones sin compartir credenciales globales.
     *
     * @param privateKey clave privada descifrada que se usara para firmar transacciones
     * @return instancia del contrato configurada con credenciales reales para escritura
     * @throws Exception si la clave privada no es valida o no se puede preparar el contrato para escritura
     */
    private RegistroDocumentos crearContratoConClave(String privateKey) throws Exception {

        String pk = privateKey.startsWith("0x")
                ? privateKey.substring(2)
                : privateKey;

        Credentials creds = Credentials.create(pk);

        RawTransactionManager txManager = new RawTransactionManager(
                web3j,
                creds,
                11155111,
                new PollingTransactionReceiptProcessor(web3j, 2000, 40)
        );

        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice()
                .multiply(BigInteger.valueOf(2));

        BigInteger gasLimit = BigInteger.valueOf(700_000);

        StaticGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);

        return RegistroDocumentos.load(
                contrato.getContractAddress(),
                web3j,
                txManager,
                gasProvider
        );
    }

    // =========================================================
    // =========================================================

    /**
     * Registra un hash en blockchain en segundo plano.
     *
     * Serializa el acceso al nonce y devuelve un Future para que la UI pueda seguir respondiendo mientras la transaccion se mina.
     *
     * @param hash hash SHA-256 del documento usado como identificador inmutable
     * @param privateKeyDesencriptada clave privada en claro de la entidad emisora que firma la operacion
     * @return future con el hash de transaccion cuando el registro se confirma correctamente
     */
    public CompletableFuture<String> registrarHashAsync(String hash, String privateKeyDesencriptada) {

        return CompletableFuture.supplyAsync(() -> {

            synchronized (nonceLock) {

                try {

                    RegistroDocumentos contratoLocal =
                            crearContratoConClave(privateKeyDesencriptada);

                    byte[] hashBytes = toBytes32(hash);

                    TransactionReceipt receipt =
                            contratoLocal.registrarDocumento(hashBytes).send();

                    if (!"0x1".equals(receipt.getStatus())) {
                        throw new RuntimeException("TX revertida");
                    }

                    return receipt.getTransactionHash();

                } catch (Exception e) {
                    throw new RuntimeException("Error registrando en blockchain", e);
                }
            }

        }, callbackPool);
    }

    // =========================================================
    // =========================================================

    /**
     * Revoca un hash en blockchain en segundo plano.
     *
     * Usa la misma proteccion de nonce que el registro porque varias transacciones firmadas por la misma clave no deben pisarse.
     *
     * @param hash hash SHA-256 del documento usado como identificador inmutable
     * @param privateKeyDesencriptada clave privada en claro de la entidad emisora que firma la operacion
     * @return future con el hash de transaccion cuando la revocacion se confirma correctamente
     */
    public CompletableFuture<String> revocarHashAsync(String hash, String privateKeyDesencriptada) {

        return CompletableFuture.supplyAsync(() -> {

            synchronized (nonceLock) {

                try {

                    RegistroDocumentos contratoLocal =
                            crearContratoConClave(privateKeyDesencriptada);

                    byte[] hashBytes = toBytes32(hash);

                    TransactionReceipt receipt =
                            contratoLocal.revocarDocumento(hashBytes).send();

                    if (!"0x1".equals(receipt.getStatus())) {
                        throw new RuntimeException("TX revertida");
                    }

                    return receipt.getTransactionHash();

                } catch (Exception e) {
                    throw new RuntimeException("Error revocando en blockchain", e);
                }
            }

        }, callbackPool);
    }

    // =========================================================
    // =========================================================

    /**
     * Consulta en blockchain los datos completos asociados a un hash.
     *
     * Convierte la respuesta del contrato a un DTO estable para que la capa superior no dependa de tuplas Web3j.
     *
     * @param hash hash SHA-256 del documento usado como identificador inmutable
     * @return llamada remota o DTO con la informacion registrada para el hash, segun la capa donde se use
     */
    public DocumentoDTO obtenerDocumento(String hash) {

        try {
            byte[] hashBytes = toBytes32(hash);

            var result = contrato.obtenerDocumento(hashBytes).send();

            String hashHex = Numeric.toHexString(result.component1());
            String fecha = String.valueOf(result.component2());
            String emisor = result.component3();

            var estadoBigInt = result.component4();

            boolean noExiste = BlockchainEstadoMapper.esNoExiste(estadoBigInt);

            String estadoFinal = noExiste
                    ? "NO_EXISTE"
                    : BlockchainEstadoMapper
                    .fromSmartContract(estadoBigInt)
                    .name();

            return new DocumentoDTO(
                    hashHex,
                    fecha,
                    emisor,
                    estadoFinal
            );

        } catch (Exception e) {

            if (e.getMessage() != null && e.getMessage().contains("No existe")) {

                return new DocumentoDTO(
                        hash,
                        null,
                        null,
                        "NO_EXISTE"
                );
            }

            throw new RuntimeException("Error consultando blockchain", e);
        }
    }

    // =========================================================
    // =========================================================

    /**
     * Comprueba de forma puntual si una transaccion existe en la red.
     *
     * Es una ayuda de diagnostico para distinguir entre errores de envio y transacciones pendientes o ausentes.
     *
     * @param txHash hash de la transaccion que se quiere comprobar en la red
     */
    public void debugTx(String txHash) {
        try {
            EthTransaction tx = web3j.ethGetTransactionByHash(txHash).send();

            if (tx.getTransaction().isPresent()) {
                System.out.println("TX en red");
            } else {
                System.out.println("TX NO en red");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Recupera todos los hashes registrados en el contrato y los transforma a hexadecimal.
     *
     * La conversion permite compararlos con los hashes calculados por la aplicacion sin manejar arrays de bytes.
     *
     * @return hashes del contrato convertidos a hexadecimal para comparacion en Java
     */
    public List<String> obtenerTodosHashesComoString() {
        try {
            List<byte[]> hashes = contrato.obtenerTodosHashes().send();
            List<String> res = new ArrayList<>();

            for (byte[] h : hashes) {
                res.add(Numeric.toHexString(h));
            }

            return res;

        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo hashes", e);
        }
    }

    /**
     * Expone la direccion real del contrato cargado por el servicio.
     *
     * Se usa para sincronizar la configuracion con el contrato operativo tras inicializar Web3j.
     *
     * @return direccion del contrato usada actualmente por el servicio
     */
    public String getContractAddress() {
        return contrato.getContractAddress();
    }

    /**
     * Adapta un hash hexadecimal al formato bytes32 exigido por el contrato Solidity.
     *
     * Valida la longitud para evitar enviar transacciones que terminarian revirtiendo por datos mal formados.
     *
     * @param hash hash SHA-256 del documento usado como identificador inmutable
     * @return hash adaptado a bytes32, formato exigido por Solidity
     */
    private byte[] toBytes32(String hash) {
        byte[] bytes = Numeric.hexStringToByteArray(hash);
        byte[] bytes32 = new byte[32];
        System.arraycopy(bytes, 0, bytes32, 0, Math.min(bytes.length, 32));
        return bytes32;
    }

    /**
     * Comprueba si una direccion de la red blockchain dispone de balance distinto de cero.
     *
     * Se utiliza como validacion previa para asegurar que la cuenta:
     * - Existe en la red (ha sido utilizada al menos una vez).
     * - Esta activa y puede operar (dispone de fondos para gas).
     *
     * No valida la existencia criptografica de la clave privada asociada,
     * ya que cualquier clave valida genera una direccion valida aunque no
     * haya sido utilizada en la red.
     *
     * @param address direccion publica de la cuenta a consultar
     * @return true si la cuenta tiene balance mayor que cero, false en caso contrario
     * @throws Exception si ocurre un error al consultar la red blockchain
     */
    public boolean tieneBalance(String address) throws Exception {
        BigInteger balance = web3j.ethGetBalance(
                address,
                DefaultBlockParameterName.LATEST
        ).send().getBalance();

        return balance.compareTo(BigInteger.ZERO) > 0;
    }
}