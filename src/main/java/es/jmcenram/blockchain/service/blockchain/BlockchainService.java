package es.jmcenram.blockchain.service.blockchain;

import es.jmcenram.blockchain.config.BlockchainConfig;
import es.jmcenram.blockchain.contract.RegistroDocumentos;
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

@Getter
public class BlockchainService {

    private static BlockchainService instance;

    private final Web3j web3j;
    private final Credentials credentials;
    private final RegistroDocumentos contrato;
    private final ExecutorService callbackPool = Executors.newFixedThreadPool(2);

    // 🔥 CLAVE: control de nonce
    private final Object nonceLock = new Object();

    public static void init(BlockchainConfig config) {
        instance = new BlockchainService(config);
    }

    public static BlockchainService getInstance() {
        if (instance == null) {
            throw new RuntimeException("BlockchainService no inicializado");
        }
        return instance;
    }

    private BlockchainService(BlockchainConfig config) {
        try {
            web3j = Web3j.build(new HttpService(config.getRpcUrl()));

            credentials = Credentials.create(config.getPrivateKey());

            System.out.println("🔑 Address: " + credentials.getAddress());

            EthGetBalance balance = web3j.ethGetBalance(
                    credentials.getAddress(),
                    DefaultBlockParameterName.LATEST
            ).send();

            System.out.println("💰 Balance: " + balance.getBalance());

            long chainId = 11155111;

            RawTransactionManager txManager = new RawTransactionManager(
                    web3j,
                    credentials,
                    chainId,
                    new PollingTransactionReceiptProcessor(web3j, 2000, 40)
            );

            // 🔥 Gas más agresivo (Sepolia necesita esto)
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice()
                    .multiply(BigInteger.valueOf(2));

            BigInteger gasLimit = BigInteger.valueOf(700_000);

            StaticGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);

            contrato = RegistroDocumentos.load(
                    config.getContractAddress(),
                    web3j,
                    txManager,
                    gasProvider
            );

            System.out.println("📄 Contrato cargado: " + contrato.getContractAddress());

        } catch (Exception e) {
            throw new RuntimeException("Error inicializando BlockchainService", e);
        }
    }

    // =========================
    // TX SEGURAS (CON NONCE LOCK)
    // =========================

    public CompletableFuture<String> registrarHashAsync(String hash) {

        return CompletableFuture.supplyAsync(() -> {

            synchronized (nonceLock) {

                try {
                    byte[] hashBytes = toBytes32(hash);

                    TransactionReceipt receipt =
                            contrato.registrarDocumento(hashBytes).send();

                    System.out.println("🚀 TX REGISTRAR: " + receipt.getTransactionHash());

                    if (!"0x1".equals(receipt.getStatus())) {
                        throw new RuntimeException("TX revertida");
                    }

                    return receipt.getTransactionHash();

                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error registrando en blockchain", e);
                }
            }

        }, callbackPool);
    }

    public CompletableFuture<String> revocarHashAsync(String hash) {

        return CompletableFuture.supplyAsync(() -> {

            synchronized (nonceLock) {

                try {
                    byte[] hashBytes = toBytes32(hash);

                    TransactionReceipt receipt =
                            contrato.revocarDocumento(hashBytes).send();

                    System.out.println("🚀 TX REVOCAR: " + receipt.getTransactionHash());

                    if (!"0x1".equals(receipt.getStatus())) {
                        throw new RuntimeException("TX revertida");
                    }

                    return receipt.getTransactionHash();

                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error revocando en blockchain", e);
                }
            }

        }, callbackPool);
    }

    // =========================
    // DEBUG
    // =========================

    public void debugTx(String txHash) {
        try {
            EthTransaction tx = web3j.ethGetTransactionByHash(txHash).send();

            if (tx.getTransaction().isPresent()) {
                System.out.println("✅ TX en red");
            } else {
                System.out.println("❌ TX NO en red");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //TODO: BORRAR
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


    //TODO: BORRAR
    public String getContractAddress() {
        return contrato.getContractAddress();
    }
    // =========================
    // HELPERS
    // =========================

    private byte[] toBytes32(String hash) {
        byte[] bytes = Numeric.hexStringToByteArray(hash);
        byte[] bytes32 = new byte[32];
        System.arraycopy(bytes, 0, bytes32, 0, Math.min(bytes.length, 32));
        return bytes32;
    }
}