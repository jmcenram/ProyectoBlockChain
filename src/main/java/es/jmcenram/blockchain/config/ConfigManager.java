package es.jmcenram.blockchain.config;

import java.io.*;
import java.util.Properties;

/**
 * Clase encargada de la gestión de la configuración de blockchain.
 *
 * Permite:
 * - Cargar configuración desde fichero
 * - Crear configuración por defecto si no existe
 * - Guardar configuración en disco
 *
 * Utiliza archivos .properties ubicados en:
 * %APPDATA%/BlockchainApp/config/blockchain.properties
 *
 * Centraliza el acceso a los parámetros:
 * - RPC URL
 * - Dirección del contrato
 *
 * @author Jcena
 * @version 1.0
 */
public class ConfigManager {

    /**
     * Carga la configuración por defecto del sistema.
     *
     * Si el fichero no existe:
     * - Se crea una configuración vacía
     * - Se guarda automáticamente en disco
     *
     * @return configuración cargada
     * @throws RuntimeException si ocurre un error durante la carga
     */
    public static BlockchainConfig load() {

        try {
            File file = new File(getConfigPath());

            if (!file.exists()) {
                BlockchainConfig config = defaultConfig();
                save(config);
                return config;
            }

            return load(file);

        } catch (Exception e) {
            throw new RuntimeException("Error cargando configuración", e);
        }
    }

    /**
     * Carga la configuración desde un fichero específico.
     *
     * @param file fichero de configuración
     * @return configuración cargada
     * @throws RuntimeException si ocurre un error durante la lectura
     */
    public static BlockchainConfig load(File file) {

        try (FileInputStream fis = new FileInputStream(file)) {

            Properties props = new Properties();
            props.load(fis);

            BlockchainConfig config = new BlockchainConfig();
            config.setRpcUrl(props.getProperty("rpcUrl", ""));
            config.setContractAddress(props.getProperty("contractAddress", ""));

            return config;

        } catch (Exception e) {
            throw new RuntimeException("Error cargando configuración desde fichero", e);
        }
    }

    /**
     * Guarda la configuración en la ruta por defecto (APPDATA).
     *
     * Si el directorio no existe, se crea automáticamente.
     *
     * @param config configuración a guardar
     * @throws RuntimeException si ocurre un error durante el guardado
     */
    public static void save(BlockchainConfig config) {

        try {
            File file = new File(getConfigPath());
            file.getParentFile().mkdirs();

            save(config, file);

        } catch (Exception e) {
            throw new RuntimeException("Error guardando configuración", e);
        }
    }

    /**
     * Guarda la configuración en un fichero específico.
     *
     * Convierte valores null a cadena vacía para evitar errores.
     *
     * @param config configuración a guardar
     * @param file fichero destino
     * @throws RuntimeException si ocurre un error durante la escritura
     */
    public static void save(BlockchainConfig config, File file) {

        try (FileOutputStream fos = new FileOutputStream(file)) {

            Properties props = new Properties();

            props.setProperty("rpcUrl", safe(config.getRpcUrl()));
            props.setProperty("contractAddress", safe(config.getContractAddress()));

            props.store(fos, "Blockchain Config");

        } catch (Exception e) {
            throw new RuntimeException("Error guardando configuración en fichero", e);
        }
    }

    /**
     * Devuelve la ruta absoluta del fichero de configuración en APPDATA.
     *
     * Ubicación:
     * - Windows: C:\Users\Usuario\AppData\Roaming\BlockchainApp\config\
     *
     * Si el directorio no existe, se crea automáticamente.
     *
     * @return ruta completa del fichero blockchain.properties
     */
    public static String getConfigPath() {

        String baseDir = System.getenv("APPDATA");

        if (baseDir == null) {
            baseDir = System.getProperty("user.home");
        }

        String configDir = baseDir + File.separator + "BlockchainApp" + File.separator + "config";

        File dir = new File(configDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return configDir + File.separator + "blockchain.properties";
    }

    /**
     * Genera una configuración por defecto vacía.
     *
     * @return configuración inicial
     */
    private static BlockchainConfig defaultConfig() {
        BlockchainConfig config = new BlockchainConfig();
        config.setRpcUrl("");
        config.setContractAddress("");
        return config;
    }

    /**
     * Convierte valores null a cadena vacía.
     *
     * Evita errores al guardar propiedades en fichero.
     *
     * @param value valor original
     * @return valor seguro (nunca null)
     */
    private static String safe(String value) {
        return value == null ? "" : value;
    }
}