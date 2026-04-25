package es.jmcenram.blockchain.service.documento;

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
import java.util.function.Consumer;

public class DocumentoService extends BaseService<Documento> {

    private final RegistroBlockchainRepository registroRepo;
    private final AuditoriaRepository auditoriaRepo;
    private final BlockchainService blockchainService;
    private final BlockchainUpdateService updateService;
    private final HashService hashService;

    public DocumentoService(BaseRepository<Documento> repository,
                            RegistroBlockchainRepository registroRepo,
                            AuditoriaRepository auditoriaRepo,
                            BlockchainService blockchainService) {
        super(repository);
        this.registroRepo = registroRepo;
        this.auditoriaRepo = auditoriaRepo;
        this.blockchainService = blockchainService;
        this.hashService = new HashService();

        // 🔥 AHORA SIN REPOS (usa JPA manual dentro)
        this.updateService = new BlockchainUpdateService();
    }

    public List<Documento> obtenerTodosConRegistros() {
        return ((DocumentoRepository) repository).obtenerTodosConRegistros();
    }

    // =========================
    // CREAR
    // =========================
    public ResultadoDocumento crearDocumentoCompletoConArchivo(
            Documento documento,
            File file,
            Usuario usuario
    ) {
        try {
            byte[] contenido = Files.readAllBytes(file.toPath());
            documento.setContenido(contenido);
            documento.setRutaArchivo(file.getAbsolutePath());

            documento.setHash(null);
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

    // =========================
    // VALIDAR
    // =========================
    public void validarDocumento(Documento documento, Usuario usuario) {

        if (documento.getEstado() != EstadoDocumento.BORRADOR) {
            throw new RuntimeException("Solo BORRADOR");
        }

        String hash = hashService.generarHash(documento.getContenido());

        documento.setHash(hash);
        documento.setEstado(EstadoDocumento.VALIDADO);

        this.guardar(documento);

        Auditoria auditoria = new Auditoria();
        auditoria.setAccion("VALIDACION_DOCUMENTO");
        auditoria.setUsuario(usuario);
        auditoria.setFechaCreacion(LocalDateTime.now());
        auditoriaRepo.save(auditoria);
    }

    // =========================
    // REGISTRAR BLOCKCHAIN
    // =========================
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

        blockchainService.registrarHashAsync(documento.getHash())
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

        blockchainService.revocarHashAsync(documento.getHash())
                .thenAccept(txHash -> {

                    System.out.println("🟠 REVOCADO TX: " + txHash);

                    try {
                        updateService.actualizarRevocacion(
                                registroId,
                                usuario,
                                txHash
                        );

                        // 🔥 CALLBACK OK
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

                    System.out.println("❌ ERROR REVOCAR");
                    ex.printStackTrace();

                    updateService.marcarError(registroId);

                    // 🔥 CALLBACK ERROR
                    if (onError != null) {
                        onError.accept(ex);
                    }

                    return null;
                });
    }

    // =========================
    // MÉTODOS ANTIGUOS (NO BORRADOS)
    // =========================

    public void actualizarRegistro(Long registroId, Long documentoId, Usuario usuario, String txHash) {
        updateService.actualizarRegistro(registroId, documentoId, usuario, txHash);
    }

    public void actualizarRevocacion(Long registroId, Usuario usuario, String txHash) {
        updateService.actualizarRevocacion(registroId, usuario, txHash);
    }

    public void marcarError(Long registroId) {
        updateService.marcarError(registroId);
    }
}