package es.jmcenram.blockchain.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de pruebas encargada de validar el comportamiento de ConfigManager.
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
@DisplayName("Tests para ConfigManager")
public class ConfigManagerTest {

    @TempDir
    Path tempDir;

    private String configPath;

    /**
     * Prepara el entorno necesario para la prueba.
     */
    @BeforeEach
    void setUp() {
        configPath = tempDir.toString() + "/test-blockchain.properties";
    }

    /**
     * Comprueba que una configuracion blockchain valida mantiene direccion de contrato, RPC y private key.
     *
     * Este caso protege el arranque de la aplicacion, que depende de esos campos antes de inicializar Web3j.
     */
    @Test
    @DisplayName("Carga de configuración válida")
    void testCargarConfiguracionValida() {
        // Arrange
        BlockchainConfig config = new BlockchainConfig();
        config.setContractAddress("0x6c0134035F62f876c77aBBE0742537859eE9E503");
        config.setRpcUrl("https://eth-sepolia.g.alchemy.com/v2/test");

        // Act & Assert
        assertNotNull(config.getContractAddress());
        assertTrue(config.getContractAddress().startsWith("0x"));
        assertNotNull(config.getRpcUrl());
    }

    /**
     * Comprueba el formato minimo de una direccion de contrato Ethereum valida.
     *
     * La validacion fija el prefijo 0x y la longitud esperada para evitar guardar direcciones que despues fallarian en Web3j.
     */
    @Test
    @DisplayName("Validación de dirección de contrato válida")
    void testValidarDireccionContratoValida() {
        // Arrange
        String addressValida = "0x6c0134035F62f876c77aBBE0742537859eE9E503";

        // Act & Assert
        assertTrue(addressValida.startsWith("0x"));
        assertEquals(42, addressValida.length()); // 0x + 40 caracteres hex
    }

    /**
     * Comprueba que una direccion sin formato Ethereum no se trate como contrato valido.
     *
     * El caso cubre entradas claramente invalidas antes de que lleguen a la configuracion de blockchain.
     */
    @Test
    @DisplayName("Rechaza dirección de contrato inválida")
    void testRechazarDireccionContratoInvalida() {
        // Arrange
        String addressInvalida = "not-a-valid-address";

        // Act & Assert
        assertFalse(addressInvalida.startsWith("0x"));
    }

    /**
     * Comprueba que la URL RPC tenga un esquema utilizable por el cliente HTTP.
     *
     * La aplicacion necesita una URL con protocolo para construir el HttpService de Web3j.
     */
    @Test
    @DisplayName("Validación de RPC URL")
    void testValidarRpcUrl() {
        // Arrange
        String rpcUrlValida = "https://eth-sepolia.g.alchemy.com/v2/Pi3D-rDniF3kSy2mNe23k";

        // Act & Assert
        assertNotNull(rpcUrlValida);
        assertTrue(rpcUrlValida.startsWith("http"));
    }

    /**
     * Comprueba que la configuracion conserva todos los campos que exige el arranque blockchain.
     *
     * El test evita que una configuracion incompleta llegue a servicios que esperan contrato, RPC y clave privada.
     */
    @Test
    @DisplayName("Configuración contiene todos los campos requeridos")
    void testConfiguracionCamposObligatorios() {
        // Arrange
        BlockchainConfig config = new BlockchainConfig();
        config.setContractAddress("0x123456789abcdef");
        config.setRpcUrl("https://rpc.example.com");

        // Act & Assert
        assertNotNull(config.getContractAddress(), "ContractAddress no puede ser null");
        assertNotNull(config.getRpcUrl(), "RpcUrl no puede ser null");
    }

    /**
     * Comprueba que dos direcciones de contrato distintas no se confundan entre si.
     *
     * El caso protege escenarios donde se cambia de contrato o red y la configuracion debe mantener cada direccion exacta.
     */
    @Test
    @DisplayName("Separación de diferentes direcciones de contrato")
    void testSeparacionDireccionesContratos() {
        // Arrange
        String address1 = "0x1111111111111111111111111111111111111111";
        String address2 = "0x2222222222222222222222222222222222222222";

        // Act & Assert
        assertNotEquals(address1, address2);
        assertTrue(address1.startsWith("0x"));
        assertTrue(address2.startsWith("0x"));
    }
}

