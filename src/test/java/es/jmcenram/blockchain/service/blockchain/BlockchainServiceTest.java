package es.jmcenram.blockchain.service.blockchain;

import es.jmcenram.blockchain.config.BlockchainConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Clase de pruebas encargada de validar el comportamiento de BlockchainService.
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
class BlockchainServiceTest {

    private BlockchainService service;

    /**
     * Prepara el entorno necesario para la prueba.
     */
    @BeforeEach
    void setUp() {
        // Reset instance for tests
        try {
            BlockchainService.class.getDeclaredField("instance").set(null, null);
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Libera los recursos utilizados por la prueba.
     */
    @AfterEach
    void tearDown() {
        // Reset instance after tests
        try {
            BlockchainService.class.getDeclaredField("instance").set(null, null);
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Comprueba que BlockchainService no permita obtener una instancia sin inicializacion previa.
     *
     * Fallar pronto deja claro que falta configuracion blockchain en lugar de provocar errores null en llamadas posteriores.
     */
    @Test
    void testGetInstance_WithoutInit_ThrowsException() {
        assertThrows(RuntimeException.class, BlockchainService::getInstance);
    }

    // Nota: El test de init requiere mocking complejo de Web3j, por ahora se omite para evitar conexiones reales
    // En un entorno real, se mockearían HttpService, Web3j, Credentials, etc.
}
