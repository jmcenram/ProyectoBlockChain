package es.jmcenram.blockchain.service.documento;

import es.jmcenram.blockchain.dto.DocumentoDTO;
import es.jmcenram.blockchain.model.auditoria.Auditoria;
import es.jmcenram.blockchain.model.documento.Documento;
import es.jmcenram.blockchain.model.documento.EstadoDocumento;
import es.jmcenram.blockchain.model.mensaje.ResultadoDocumento;
import es.jmcenram.blockchain.model.registroblockchain.EstadoBlockchain;
import es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain;
import es.jmcenram.blockchain.model.usuario.Usuario;

import es.jmcenram.blockchain.repository.auditoria.AuditoriaRepository;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import es.jmcenram.blockchain.repository.documento.DocumentoRepository;
import es.jmcenram.blockchain.repository.registroblockchain.RegistroBlockchainRepository;

import es.jmcenram.blockchain.service.base.BaseService;
import es.jmcenram.blockchain.service.blockchain.BlockchainService;
import es.jmcenram.blockchain.service.blockchain.BlockchainUpdateService;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Servicio encargado de la logica de negocio de Documento.
 *
 * Permite:
 * - Validar reglas antes de persistir cambios
 * - Coordinar repositorios relacionados
 * - Exponer operaciones usadas por controladores u otros servicios
 *
 * Forma parte de la capa de servicio y mantiene la logica fuera de la interfaz.
 *
 * @author Jcena
 * @version 1.0
 */
public class DocumentoService extends BaseService<Documento> {

    /** Repositorio para operaciones de RegistroBlockchain */
    private final RegistroBlockchainRepository registroRepo;

    /** Repositorio para registros de auditoría */
    private final AuditoriaRepository auditoriaRepo;

    /** Servicio blockchain para registro y revocación de hashes */
    private final BlockchainService blockchainService;

    /** Servicio para actualizar BD después de transacciones blockchain */
    private final BlockchainUpdateService updateService;

    /** Servicio para generar hashes SHA-256 de documentos */
    private final HashService hashService;

    /**
     * Constructor que inicializa el servicio con sus dependencias.
     * Crea automaticamente BlockchainUpdateService para coordinación.
     *
     * @param repository repositorio genérico de Documento
     * @param registroRepo repositorio de RegistroBlockchain
     * @param auditoriaRepo repositorio de auditoriai
     * @param blockchainService servicio de blockchain ya inicializado
     */
    public DocumentoService(BaseRepository<Documento> repository,
                            RegistroBlockchainRepository registroRepo,
                            AuditoriaRepository auditoriaRepo,
                            BlockchainService blockchainService) {
        super(repository);
        this.registroRepo = registroRepo;
        this.auditoriaRepo = auditoriaRepo;
        this.blockchainService = blockchainService;
        this.hashService = new HashService();

        // AHORA SIN REPOS (usa JPA manual dentro)
        this.updateService = new BlockchainUpdateService();
    }

    /**
     * Obtiene todos los documentos con sus registros blockchain asociados (eager loading).
     *
     * @return lista de documentos con registros cargados
     */
    public List<Documento> obtenerTodosConRegistros() {
        return ((DocumentoRepository) repository).obtenerTodosConRegistros();
    }

    /**
     * Crea un nuevo documento en estado BORRADOR a partir de un archivo.
     * Lee el contenido del archivo, asigna usuario emisor y estado inicial.
     * Crea registro de auditoría con acción "CREACION_DOCUMENTO".
     *
     * @param documento documento con metadatos (nombre, tipo, etc.)
     * @param file archivo del sistema de archivos a registrar
     * @param usuario usuario que crea el documento
     * @return ResultadoDocumento con documento guardado y mensaje de éxito
     * @throws RuntimeException si hay error al leer el archivo o guardar en BD
     */
    public ResultadoDocumento crearDocumentoCompletoConArchivo(
            Documento documento,
            File file,
            Usuario usuario
    ) {
        try {
            byte[] contenido = Files.readAllBytes(file.toPath());
            String hash = hashService.generarHash(contenido);
            Documento existente = buscarDocumentoDuplicado(hash, documento.getId());

            if (existente != null) {
                ResultadoDocumento resultado = new ResultadoDocumento();
                resultado.setDocumento(existente);
                resultado.setDuplicado(true);
                resultado.setMensaje("Documento duplicado");
                return resultado;
            }

            documento.setContenido(contenido);
            documento.setRutaArchivo(file.getAbsolutePath());

            documento.setHash(hash);
            documento.setEstado(EstadoDocumento.BORRADOR);
            documento.setEmisor(usuario);

            Documento docGuardado = this.guardar(documento);

            Auditoria auditoria = new Auditoria();
            auditoria.setAccion("CREACION_DOCUMENTO");
            auditoria.setUsuario(usuario);
            auditoria.setFechaCreacion(LocalDateTime.now());
            auditoriaRepo.save(auditoria);

            ResultadoDocumento resultado = new ResultadoDocumento();
            resultado.setDocumento(docGuardado);
            resultado.setMensaje("Documento creado en BORRADOR");

            return resultado;

        } catch (Exception e) {
            throw new RuntimeException("Error creando documento", e);
        }
    }

    /**
     * Valida un documento generando su hash SHA-256 y cambiando estado a VALIDADO.
     * Solo permite validar documentos en estado BORRADOR.
     * Calcula hash del contenido del documento y lo almacena.
     * Crea registro de auditoría con acción "VALIDACION_DOCUMENTO".
     *
     * @param documento documento a validar (debe estar en estado BORRADOR)
     * @param usuario usuario que realiza la validación
     * @throws RuntimeException si el documento no está en estado BORRADOR
     */
    public void validarDocumento(Documento documento, Usuario usuario) {

        if (documento.getEstado() != EstadoDocumento.BORRADOR) {
            throw new RuntimeException("Solo BORRADOR");
        }

        String hash = hashService.generarHash(documento.getContenido());
        Documento existente = buscarDocumentoDuplicado(hash, documento.getId());

        if (existente != null) {
            throw new RuntimeException("Documento duplicado");
        }

        documento.setHash(hash);
        documento.setEstado(EstadoDocumento.VALIDADO);

        this.guardar(documento);

        Auditoria auditoria = new Auditoria();
        auditoria.setAccion("VALIDACION_DOCUMENTO");
        auditoria.setUsuario(usuario);
        auditoria.setFechaCreacion(LocalDateTime.now());
        auditoriaRepo.save(auditoria);
    }

    /**
     * Registra el hash del documento en la blockchain de forma asincrónica.
     * Crea RegistroBlockchain con estado PENDIENTE.
     * Invoca BlockchainService.registrarHashAsync y usa callbacks para actualizar BD.
     *
     * Flujo asincrónico:
     * 1. Crea RegistroBlockchain con estado PENDIENTE
     * 2. Envía hash a blockchain (no espera, es async)
     * 3. Si éxito: onSuccess callback y actualiza con txHash
     * 4. Si error: onError callback y marca como ERROR
     *
     * Callbacks son opcionales (null-safe).
     *
     * @param documento documento con hash validado
     * @param usuario usuario que registra el documento
     * @param onSuccess callback si transacción exitosa, recibe txHash
     * @param onError callback si transacción falla, recibe excepción
     */
    public void registrarEnBlockchain(
            Documento documento,
            Usuario usuario,
            Consumer<String> onSuccess,
            Consumer<Throwable> onError
    ) {

        RegistroBlockchain registro = new RegistroBlockchain();
        registro.setDocumento(documento);
        registro.setHashDocumento(documento.getHash());
        registro.setDireccionContrato(blockchainService.getContractAddress());
        registro.setFechaCreacion(LocalDateTime.now());
        registro.setEstado(EstadoBlockchain.PENDIENTE);

        registro = registroRepo.save(registro);

        Long registroId = registro.getId();
        Long documentoId = documento.getId();

        blockchainService.registrarHashAsync(documento.getHash(), usuario.getEntidadEmisora().getPrivateKeyDecrypted())
                .thenAccept(txHash -> {

                    updateService.actualizarRegistro(
                            registroId,
                            documentoId,
                            usuario,
                            txHash
                    );

                    if (onSuccess != null) onSuccess.accept(txHash);

                })
                .exceptionally(ex -> {

                    updateService.marcarError(registroId);

                    if (onError != null) onError.accept(ex);

                    return null;
                });
    }

    /**
     * Revoca (anula) el registro del documento en la blockchain de forma asincrónica.
     * Crea un nuevo RegistroBlockchain con estado PENDIENTE para la revocación.
     * Invoca BlockchainService.revocarHashAsync y coordina actualización de BD.
     *
     * Flujo asincrónico:
     * 1. Crea RegistroBlockchain para la operación de revocación
     * 2. Envía hash a blockchain para revocar (no espera, es async)
     * 3. Si éxito: actualiza registro a REVOCADO y llama onSuccess
     * 4. Si error: marca como ERROR y llama onError
     *
     * Callbacks son opcionales (null-safe).
     *
     * @param documento documento cuyo registro será revocado
     * @param usuario usuario que realiza la revocación
     * @param onSuccess callback si revocación exitosa, recibe txHash
     * @param onError callback si revocación falla, recibe excepción
     */
    public void revocarDocumento(
            Documento documento,
            Usuario usuario,
            Consumer<String> onSuccess,
            Consumer<Throwable> onError
    ) {

        RegistroBlockchain registro = new RegistroBlockchain();
        registro.setDocumento(documento);
        registro.setHashDocumento(documento.getHash());
        registro.setDireccionContrato(blockchainService.getContractAddress());
        registro.setFechaCreacion(LocalDateTime.now());
        registro.setEstado(EstadoBlockchain.PENDIENTE);

        registro = registroRepo.save(registro);

        Long registroId = registro.getId();

        blockchainService.revocarHashAsync(documento.getHash(), usuario.getEntidadEmisora().getPrivateKeyDecrypted())
                .thenAccept(txHash -> {

                    System.out.println("REVOCADO TX: " + txHash);

                    try {
                        updateService.actualizarRevocacion(
                                registroId,
                                usuario,
                                txHash
                        );

                        // CALLBACK OK
                        if (onSuccess != null) {
                            onSuccess.accept(txHash);
                        }

                    } catch (Exception e) {

                        e.printStackTrace();
                        updateService.marcarError(registroId);

                        if (onError != null) {
                            onError.accept(e);
                        }
                    }

                })
                .exceptionally(ex -> {

                    System.out.println("ERROR REVOCAR");
                    ex.printStackTrace();

                    updateService.marcarError(registroId);

                    // CALLBACK ERROR
                    if (onError != null) {
                        onError.accept(ex);
                    }

                    return null;
                });
    }

    /**
     * Delegación a BlockchainUpdateService para actualizar registro en BD (método heredado).
     * Mantiene referencia para compatibilidad con código antiguo.
     *
     * @param registroId ID del RegistroBlockchain
     * @param documentoId ID del Documento
     * @param usuario usuario para auditoría
     * @param txHash hash de transacción blockchain
     */
    public void actualizarRegistro(Long registroId, Long documentoId, Usuario usuario, String txHash) {
        updateService.actualizarRegistro(registroId, documentoId, usuario, txHash);
    }

    /**
     * Delegación a BlockchainUpdateService para actualizar revocación en BD (método heredado).
     * Mantiene referencia para compatibilidad con código antiguo.
     *
     * @param registroId ID del RegistroBlockchain
     * @param usuario usuario para auditoría
     * @param txHash hash de transacción blockchain
     */
    public void actualizarRevocacion(Long registroId, Usuario usuario, String txHash) {
        updateService.actualizarRevocacion(registroId, usuario, txHash);
    }

    /**
     * Delegación a BlockchainUpdateService para marcar error (método heredado).
     * Mantiene referencia para compatibilidad con código antiguo.
     *
     * @param registroId ID del RegistroBlockchain con error
     */
    public void marcarError(Long registroId) {
        updateService.marcarError(registroId);
    }

    /**
     * Busca en la base de datos local el documento asociado a un hash.
     *
     * Este acceso permite enlazar una verificacion blockchain con el registro funcional almacenado por la aplicacion.
     *
     * @param hash hash SHA-256 del documento usado como identificador inmutable
     * @return documento local que coincide con el hash, o null si no existe
     */
    public Documento buscarPorHash(String hash) {
        return ((DocumentoRepository) repository).findByHash(hash);
    }

    private Documento buscarDocumentoDuplicado(String hash, Long documentoActualId) {
        Documento existente = buscarPorHash(hash);

        if (existente != null) {
            if (documentoActualId != null && Objects.equals(existente.getId(), documentoActualId)) {
                return null;
            }

            return existente;
        }

        List<Documento> documentosSinHash = ((DocumentoRepository) repository).findSinHash();
        if (documentosSinHash == null || documentosSinHash.isEmpty()) {
            return null;
        }

        return documentosSinHash.stream()
                .filter(doc -> documentoActualId == null || !Objects.equals(doc.getId(), documentoActualId))
                .filter(doc -> doc.getContenido() != null)
                .filter(doc -> hash.equals(hashService.generarHash(doc.getContenido())))
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica un documento combinando informacion local y estado blockchain.
     *
     * Devuelve un resultado de dominio para que la UI pueda mostrar si el documento existe, esta registrado o fue revocado.
     *
     * @param hash hash SHA-256 del documento usado como identificador inmutable
     * @return resultado de verificacion usado por la capa superior para decidir el estado del documento
     */
    public ResultadoDocumento verificarDocumento(String hash) {

        try {

            ResultadoDocumento resultado = new ResultadoDocumento();

            // 1. Consultar blockchain (fuente de verdad externa)
            DocumentoDTO docBC = blockchainService.obtenerDocumento(hash);

            // 2. Si no existe en blockchain
            if ("NO_EXISTE".equals(docBC.getEstado())) {

                resultado.setMensaje("El documento NO existe en blockchain");
                resultado.setEstadoBlockchain("NO_EXISTE");

                return resultado;
            }

            // 3. Buscar documento en BD
            Documento docBD = buscarPorHash(hash);

            if (docBD == null) {

                resultado.setMensaje("Existe en blockchain pero NO en base de datos");
                resultado.setEstadoBlockchain(docBC.getEstado());

                return resultado;
            }

            // 4. Obtener registros blockchain del documento
            List<RegistroBlockchain> registros = docBD.getRegistros();

            if (registros == null || registros.isEmpty()) {

                resultado.setDocumento(docBD);
                resultado.setEstadoBlockchain(docBC.getEstado());
                resultado.setMensaje("Documento sin registros locales, pero existe en blockchain");

                return resultado;
            }

            // 5. Filtrar por contrato activo
            String contratoActual = blockchainService.getContractAddress();

            RegistroBlockchain ultimoRegistro = registros.stream()
                    .filter(r -> contratoActual.equals(r.getDireccionContrato()))
                    .max((r1, r2) -> r1.getFechaCreacion().compareTo(r2.getFechaCreacion()))
                    .orElse(null);

            // 6. Si no hay registros del contrato actual
            if (ultimoRegistro == null) {

                resultado.setDocumento(docBD);
                resultado.setEstadoBlockchain(docBC.getEstado());
                resultado.setMensaje("No hay registros para el contrato actual");

                return resultado;
            }

            // 7. Construir resultado final
            resultado.setDocumento(docBD);
            resultado.setEstadoBlockchain(docBC.getEstado());
            resultado.setTxHash(ultimoRegistro.getTransactionHash());

            if (ultimoRegistro.getFechaCreacion() != null) {
                resultado.setFechaBlockchain(ultimoRegistro.getFechaCreacion().toString());
            }

            resultado.setMensaje("Documento verificado correctamente");

            return resultado;

        } catch (Exception e) {
            throw new RuntimeException("Error verificando documento", e);
        }
    }
}
