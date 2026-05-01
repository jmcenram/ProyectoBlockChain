package es.jmcenram.blockchain.service.entidademisora;

import es.jmcenram.blockchain.model.entidademisora.EntidadEmisora;
import es.jmcenram.blockchain.repository.entidademisora.EntidadEmisoraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas encargada de validar el comportamiento de EntidadEmisoraService.
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
class EntidadEmisoraServiceTest {

    @Mock
    private EntidadEmisoraRepository repository;

    private EntidadEmisoraService service;

    /**
     * Prepara el entorno necesario para la prueba.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new EntidadEmisoraService(repository);
    }

    /**
     * Comprueba que la creacion de entidad emisora valide datos y persista.
     *
     * El servicio debe verificar que nombre, address y privateKey cumplan longitudes minimas
     * antes de guardar en base de datos.
     */
    @Test
    void testCrearEntidad_Success() {
        // Given
        String nombre = "Entidad Test";
        String address = "0x123456789012345678901234567890123456789a";
        String privateKey = "0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        EntidadEmisora entidad = new EntidadEmisora();
        entidad.setNombre(nombre);
        entidad.setAddress(address);
        entidad.setPrivateKey(privateKey);
        when(repository.existsByNombre(nombre)).thenReturn(false);
        when(repository.existsByAddress(address)).thenReturn(false);
        when(repository.save(any(EntidadEmisora.class))).thenReturn(entidad);

        // When
        EntidadEmisora result = service.crearEntidad(nombre, address, privateKey);

        // Then
        assertNotNull(result);
        assertEquals(nombre, result.getNombre());
        assertTrue(result.getActivo());
        verify(repository).existsByNombre(nombre);
        verify(repository).existsByAddress(address);
        verify(repository).save(any(EntidadEmisora.class));
    }

    /**
     * Comprueba que no se cree entidad cuando el nombre ya existe.
     *
     * La unicidad del nombre previene conflictos de identidad entre emisores.
     */
    @Test
    void testCrearEntidad_NombreDuplicado() {
        // Given
        String nombre = "Entidad Existente";
        String address = "0x123456789012345678901234567890123456789a";
        String privateKey = "0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        when(repository.existsByNombre(nombre)).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class,
            () -> service.crearEntidad(nombre, address, privateKey));
    }

    /**
     * Comprueba que no se cree entidad cuando el address ya existe.
     *
     * Evita asociar multiples registros a la misma dirección de wallet.
     */
    @Test
    void testCrearEntidad_AddressDuplicado() {
        // Given
        String nombre = "Entidad Nueva";
        String address = "0xDUPLICADO0000000000000000000000000000000a";
        String privateKey = "0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        when(repository.existsByNombre(nombre)).thenReturn(false);
        when(repository.existsByAddress(address)).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class,
            () -> service.crearEntidad(nombre, address, privateKey));
    }

    /**
     * Comprueba que nombre vacio sea rechazado.
     *
     * Previene cambios futuros de identificacion al estar vacio.
     */
    @Test
    void testCrearEntidad_NombreVacio() {
        // Given
        String nombre = "";
        String address = "0x123456789012345678901234567890123456789a";
        String privateKey = "0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

        // When & Then
        assertThrows(RuntimeException.class,
            () -> service.crearEntidad(nombre, address, privateKey));
    }

    /**
     * Comprueba que address muy corto sea rechazado.
     *
     * Las direcciones de wallet tienen una longitud fija esperada en blockchain.
     */
    @Test
    void testCrearEntidad_AddressCorto() {
        // Given
        String nombre = "Entidad Test";
        String address = "0x123"; // Demasiado corto
        String privateKey = "0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

        // When & Then
        assertThrows(RuntimeException.class,
            () -> service.crearEntidad(nombre, address, privateKey));
    }

    /**
     * Comprueba que clave privada muy corta sea rechazada.
     *
     * La seguridad criptografica depende de una longitud minima de clave.
     */
    @Test
    void testCrearEntidad_PrivateKeyCorta() {
        // Given
        String nombre = "Entidad Test";
        String address = "0x123456789012345678901234567890123456789a";
        String privateKey = "0x123"; // Demasiado corta

        // When & Then
        assertThrows(RuntimeException.class,
            () -> service.crearEntidad(nombre, address, privateKey));
    }

    /**
     * Comprueba que se encuentre una entidad por nombre valido.
     *
     * La busqueda por nombre es una operacion comun en la UI para seleccionar emisores.
     */
    @Test
    void testObtenerPorNombre_Success() {
        // Given
        String nombre = "Test Emisor";
        EntidadEmisora entidad = new EntidadEmisora();
        entidad.setNombre(nombre);
        when(repository.findByNombre(nombre)).thenReturn(entidad);

        // When
        EntidadEmisora result = service.obtenerPorNombre(nombre);

        // Then
        assertNotNull(result);
        assertEquals(nombre, result.getNombre());
        verify(repository).findByNombre(nombre);
    }

    /**
     * Comprueba que nombre no encontrado lance excepcion.
     *
     * La distincion entre nombre vacio y no encontrado ayuda a debugging.
     */
    @Test
    void testObtenerPorNombre_NoEncontrado() {
        // Given
        String nombre = "Inexistente";
        when(repository.findByNombre(nombre)).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class,
            () -> service.obtenerPorNombre(nombre));
    }

    /**
     * Comprueba que se encuentre una entidad por address valido.
     *
     * Necesario para resolver transacciones blockchain a su emisor registrado.
     */
    @Test
    void testObtenerPorAddress_Success() {
        // Given
        String address = "0xABCD1234567890ABCD1234567890ABCD1234567890";
        EntidadEmisora entidad = new EntidadEmisora();
        entidad.setAddress(address);
        when(repository.findByAddress(address)).thenReturn(entidad);

        // When
        EntidadEmisora result = service.obtenerPorAddress(address);

        // Then
        assertNotNull(result);
        assertEquals(address, result.getAddress());
        verify(repository).findByAddress(address);
    }

    /**
     * Comprueba que se encuentre una entidad por clave privada.
     *
     * Operacion sensible restringida a administradores para operaciones criptograficas.
     */
    @Test
    void testObtenerPorPrivateKey_Success() {
        // Given
        String privateKey = "0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        EntidadEmisora entidad = new EntidadEmisora();
        entidad.setPrivateKey(privateKey);
        when(repository.findByPrivateKey(privateKey)).thenReturn(entidad);

        // When
        EntidadEmisora result = service.obtenerPorPrivateKey(privateKey);

        // Then
        assertNotNull(result);
        assertEquals(privateKey, result.getPrivateKey());
        verify(repository).findByPrivateKey(privateKey);
    }

    /**
     * Comprueba que se obtengan todas las entidades emisoras activas.
     *
     * Usado por controladores para poblar listas y selectores en la UI.
     */
    @Test
    void testObtenerTodasActivas() {
        // Given
        EntidadEmisora entidad1 = new EntidadEmisora();
        entidad1.setNombre("Emisor 1");
        EntidadEmisora entidad2 = new EntidadEmisora();
        entidad2.setNombre("Emisor 2");
        List<EntidadEmisora> entidades = Arrays.asList(entidad1, entidad2);
        when(repository.findAllActivas()).thenReturn(entidades);

        // When
        List<EntidadEmisora> result = service.obtenerTodasActivas();

        // Then
        assertEquals(2, result.size());
        verify(repository).findAllActivas();
    }

    /**
     * Comprueba que se active una entidad por su ID.
     *
     * Permite reactivar emisores desactivados previamente.
     */
    @Test
    void testActivar_Success() {
        // Given
        Long id = 1L;
        EntidadEmisora entidad = new EntidadEmisora();
        entidad.setId(id);
        entidad.setActivo(false);
        when(repository.findById(id)).thenReturn(entidad);
        when(repository.update(any(EntidadEmisora.class))).thenReturn(entidad);

        // When
        EntidadEmisora result = service.activar(id);

        // Then
        assertNotNull(result);
        verify(repository).findById(id);
        verify(repository).update(any(EntidadEmisora.class));
    }

    /**
     * Comprueba que ID nulo sea rechazado antes de consultar.
     *
     * Valida la entrada obligatoria antes de operaciones de persistencia.
     */
    @Test
    void testActivar_IdNulo() {
        // Given
        Long id = null;

        // When & Then
        assertThrows(RuntimeException.class,
            () -> service.activar(id));
    }

    /**
     * Comprueba que se desactive una entidad por su ID.
     *
     * Permite inhabilitar emisores sin eliminar su registro historico.
     */
    @Test
    void testDesactivar_Success() {
        // Given
        Long id = 1L;
        EntidadEmisora entidad = new EntidadEmisora();
        entidad.setId(id);
        entidad.setActivo(true);
        when(repository.findById(id)).thenReturn(entidad);
        when(repository.update(any(EntidadEmisora.class))).thenReturn(entidad);

        // When
        EntidadEmisora result = service.desactivar(id);

        // Then
        assertNotNull(result);
        verify(repository).findById(id);
        verify(repository).update(any(EntidadEmisora.class));
    }

    /**
     * Comprueba que se actualice una entidad existente.
     *
     * Permite modificar datos despues de creacion, validando unicidad de nuevos valores.
     */
    @Test
    void testActualizar_Success() {
        // Given
        Long id = 1L;
        String nombreNuevo = "Nuevo Nombre";
        String addressNuevo = "0xNEWADDRESS000000000000000000000000000000";
        String privateKeyNueva = "0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        EntidadEmisora entidad = new EntidadEmisora();
        entidad.setId(id);
        entidad.setNombre("Nombre Viejo");
        entidad.setAddress("0xOLDADDRESS0000000000000000000000000000000");
        when(repository.findById(id)).thenReturn(entidad);
        when(repository.existsByNombre(nombreNuevo)).thenReturn(false);
        when(repository.update(any(EntidadEmisora.class))).thenReturn(entidad);

        // When
        EntidadEmisora result = service.actualizar(id, nombreNuevo, addressNuevo, privateKeyNueva, true);

        // Then
        assertNotNull(result);
        verify(repository).findById(id);
        verify(repository).update(any(EntidadEmisora.class));
    }

    /**
     * Comprueba que actualizar con nombre duplicado lance excepcion.
     *
     * Protege la unicidad del identificador incluso tras cambios.
     */
    @Test
    void testActualizar_NombreDuplicado() {
        // Given
        Long id = 1L;
        String nombreNuevo = "Nombre Existente";
        String addressNuevo = "0xNEWADDRESS000000000000000000000000000000";
        String privateKeyNueva = "0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        EntidadEmisora entidad = new EntidadEmisora();
        entidad.setId(id);
        entidad.setNombre("Nombre Viejo");
        when(repository.findById(id)).thenReturn(entidad);
        when(repository.existsByNombre(nombreNuevo)).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class,
            () -> service.actualizar(id, nombreNuevo, addressNuevo, privateKeyNueva, true));
    }

    /**
     * Comprueba que se verifique la existencia de nombre.
     *
     * Permite validar disponibilidad antes de crear o actualizar.
     */
    @Test
    void testExisteNombre() {
        // Given
        String nombre = "Emisor Test";
        when(repository.existsByNombre(nombre)).thenReturn(true);

        // When
        boolean result = service.existeNombre(nombre);

        // Then
        assertTrue(result);
        verify(repository).existsByNombre(nombre);
    }

    /**
     * Comprueba que se verifique la existencia de address.
     *
     * Necesario para prevenir duplicados de wallet en blockchain.
     */
    @Test
    void testExisteAddress() {
        // Given
        String address = "0xABCD1234567890ABCD1234567890ABCD1234567890";
        when(repository.existsByAddress(address)).thenReturn(true);

        // When
        boolean result = service.existeAddress(address);

        // Then
        assertTrue(result);
        verify(repository).existsByAddress(address);
    }

    /**
     * Comprueba que save delegue correctamente en guardar.
     *
     * Wrapper esperado por el controlador para interface consistente.
     */
    @Test
    void testSave() {
        // Given
        EntidadEmisora entidad = new EntidadEmisora();
        entidad.setNombre("Test");
        when(repository.save(entidad)).thenReturn(entidad);

        // When
        EntidadEmisora result = service.save(entidad);

        // Then
        assertNotNull(result);
        verify(repository).save(entidad);
    }

    /**
     * Comprueba que delete elimine la entidad.
     *
     * Wrapper esperado por el controlador para eliminacion logica.
     */
    @Test
    void testDelete() {
        // Given
        EntidadEmisora entidad = new EntidadEmisora();
        entidad.setId(1L);

        // When
        service.delete(entidad);

        // Then
        verify(repository).softDelete(1L);
    }

    /**
     * Comprueba que findAllActivas delegue correctamente.
     *
     * Wrapper esperado por el controlador para cargar entidades activas.
     */
    @Test
    void testFindAllActivas() {
        // Given
        List<EntidadEmisora> entidades = Arrays.asList(new EntidadEmisora(), new EntidadEmisora());
        when(repository.findAllActivas()).thenReturn(entidades);

        // When
        List<EntidadEmisora> result = service.findAllActivas();

        // Then
        assertEquals(2, result.size());
        verify(repository).findAllActivas();
    }
}


