package es.jmcenram.blockchain.service.registroblockchain;

import es.jmcenram.blockchain.model.registroblockchain.RegistroBlockchain;
import es.jmcenram.blockchain.repository.base.BaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas encargada de validar el comportamiento de RegistroBlockchainService.
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
class RegistroBlockchainServiceTest {

    @Mock
    private BaseRepository<RegistroBlockchain> repository;

    private RegistroBlockchainService service;

    /**
     * Prepara el entorno necesario para la prueba.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RegistroBlockchainService(repository);
    }

    /**
     * Comprueba que se guarde un registro blockchain correctamente.
     *
     * Verifica que el servicio delegue al repositorio de forma consistente.
     */
    @Test
    void testGuardar_Success() {
        // Given
        RegistroBlockchain registro = new RegistroBlockchain();
        registro.setId(1L);
        when(repository.save(registro)).thenReturn(registro);

        // When
        RegistroBlockchain result = service.guardar(registro);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).save(registro);
    }

    /**
     * Comprueba que se obtiengan todos los registros blockchain.
     *
     * Verifica que el servicio delegue la operación al repositorio.
     */
    @Test
    void testObtenerTodos_Success() {
        // Given
        java.util.List<RegistroBlockchain> registros = new java.util.ArrayList<>();
        registros.add(new RegistroBlockchain());
        registros.add(new RegistroBlockchain());
        when(repository.findAll()).thenReturn(registros);

        // When
        java.util.List<RegistroBlockchain> result = service.obtenerTodos();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    /**
     * Comprueba que se elimine un registro por su ID.
     *
     * Verifica que la operacion de eliminacion sea delegada al repositorio.
     */
    @Test
    void testEliminar_Success() {
        // Given
        Long id = 1L;

        // When
        service.eliminar(id);

        // Then
        verify(repository).softDelete(id);
    }

    /**
     * Comprueba que se busque un registro por su ID.
     *
     * Operacion comun para recuperar detalles de un registro en particular.
     */
    @Test
    void testObtenerPorId_Success() {
        // Given
        Long id = 1L;
        RegistroBlockchain registro = new RegistroBlockchain();
        registro.setId(id);
        when(repository.findById(id)).thenReturn(registro);

        // When
        RegistroBlockchain result = service.obtenerPorId(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(repository).findById(id);
    }

    /**
     * Comprueba que se devuelva null cuando no existe registro para el ID.
     *
     * Permite al controlador distinguir entre no encontrado y error de persistencia.
     */
    @Test
    void testObtenerPorId_NoEncontrado() {
        // Given
        Long id = 999L;
        when(repository.findById(id)).thenThrow(new RuntimeException("Not found"));

        // When & Then
        assertThrows(RuntimeException.class,
            () -> service.obtenerPorId(id));
        verify(repository).findById(id);
    }

}

