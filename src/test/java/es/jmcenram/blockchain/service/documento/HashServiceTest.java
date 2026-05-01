package es.jmcenram.blockchain.service.documento;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de pruebas encargada de validar el comportamiento de HashService.
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
class HashServiceTest {

    private final HashService hashService = new HashService();

    /**
     * Comprueba que el hash generado para contenido normal tenga formato SHA-256 hexadecimal.
     *
     * La longitud y los caracteres validan que la huella puede guardarse y compararse de forma estable.
     */
    @Test
    void testGenerarHash() {
        // Given
        byte[] data = "test data".getBytes();

        // When
        String hash = hashService.generarHash(data);

        // Then
        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA-256 produces 64 hex characters
        assertTrue(hash.matches("[a-f0-9]+"));
    }

    /**
     * Comprueba que un contenido vacio tambien produzca un hash SHA-256 valido.
     *
     * El algoritmo debe ser determinista incluso para entradas sin bytes, evitando casos especiales en servicios superiores.
     */
    @Test
    void testGenerarHash_EmptyData() {
        // Given
        byte[] data = new byte[0];

        // When
        String hash = hashService.generarHash(data);

        // Then
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    /**
     * Comprueba que la misma entrada genere siempre el mismo hash.
     *
     * La verificacion documental depende de que la huella sea reproducible entre carga, consulta y validacion.
     */
    @Test
    void testGenerarHash_Consistency() {
        // Given
        byte[] data = "consistent".getBytes();

        // When
        String hash1 = hashService.generarHash(data);
        String hash2 = hashService.generarHash(data);

        // Then
        assertEquals(hash1, hash2);
    }
}
