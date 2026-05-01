package es.jmcenram.blockchain.service.documento;

import es.jmcenram.blockchain.model.documento.Documento;
import es.jmcenram.blockchain.model.documento.EstadoDocumento;
import es.jmcenram.blockchain.model.mensaje.ResultadoDocumento;
import es.jmcenram.blockchain.model.usuario.Usuario;
import es.jmcenram.blockchain.repository.auditoria.AuditoriaRepository;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import es.jmcenram.blockchain.repository.documento.DocumentoRepository;
import es.jmcenram.blockchain.repository.registroblockchain.RegistroBlockchainRepository;
import es.jmcenram.blockchain.service.blockchain.BlockchainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas encargada de validar el comportamiento de DocumentoService.
 *
 * Verifica:
 * - Casos correctos esperados
 * - Entradas invalidas o errores controlados
 * - Colaboracion con dependencias mockeadas cuando aplica
 *
 * Forma parte de la suite de pruebas automatizadas del proyecto.
 *
 * @author Jcena
 * @version 1.0
 */
class DocumentoServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;
    @Mock
    private RegistroBlockchainRepository registroRepo;
    @Mock
    private AuditoriaRepository auditoriaRepo;
    @Mock
    private BlockchainService blockchainService;

    private DocumentoService documentoService;

    /**
     * Prepara el entorno necesario para la prueba.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        documentoService = new DocumentoService(documentoRepository, registroRepo, auditoriaRepo, blockchainService);
    }

    /**
     * Comprueba que el servicio devuelva documentos junto con sus registros blockchain asociados.
     *
     * El listado de documentos necesita esa relacion para pintar estado y fecha de registro en la tabla.
     */
    @Test
    void testObtenerTodosConRegistros() {
        // Given
        List<Documento> documentos = Arrays.asList(new Documento(), new Documento());
        when(documentoRepository.obtenerTodosConRegistros()).thenReturn(documentos);

        // When
        List<Documento> result = documentoService.obtenerTodosConRegistros();

        // Then
        assertEquals(documentos, result);
        verify(documentoRepository).obtenerTodosConRegistros();
    }

    /**
     * Comprueba la creacion completa de un documento a partir de un archivo valido.
     *
     * El caso cubre almacenamiento, hash y persistencia, que son pasos previos al registro blockchain.
     *
     * @throws Exception si la preparacion del archivo temporal o la operacion probada falla durante el escenario
     */
    @Test
    void testCrearDocumentoCompletoConArchivo_Success() throws Exception {
        // Given
        Documento documento = new Documento();
        File file = mock(File.class);
        when(file.getAbsolutePath()).thenReturn("/path/to/file");

        Usuario usuario = new Usuario();
        when(documentoRepository.save(any(Documento.class))).thenReturn(documento);
        when(auditoriaRepo.save(any())).thenReturn(null);

        // Mock Files.readAllBytes statically
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.readAllBytes(any(Path.class))).thenReturn("content".getBytes());

            // When
            ResultadoDocumento result = documentoService.crearDocumentoCompletoConArchivo(documento, file, usuario);

            // Then
            assertNotNull(result);
            assertEquals(documento, result.getDocumento());
            assertEquals(EstadoDocumento.BORRADOR, documento.getEstado());
            verify(documentoRepository).save(any(Documento.class));
            verify(auditoriaRepo).save(any());
        }
    }

    // Additional tests can be added for validarDocumento, etc.
}
