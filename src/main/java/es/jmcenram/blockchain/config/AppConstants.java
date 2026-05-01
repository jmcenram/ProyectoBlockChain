package es.jmcenram.blockchain.config;

/**
 * Clase de constantes globales utilizadas por la aplicacion.
 *
 * Centraliza valores compartidos como rutas, nombres de recursos o claves de configuracion para evitar literales duplicados.
 *
 * Se utiliza desde distintas capas cuando un valor debe mantenerse estable y coherente.
 *
 * @author Jcena
 * @version 1.0
 */
public final class AppConstants {

    /**
     * Constructor privado para evitar instanciación de la clase utilitaria.
     */
    private AppConstants() {
        // Evita instanciación
    }

    /** Nombre de la unidad de persistencia JPA definida en persistence.xml */
    public static final String PERSISTENCE_UNIT = "blockchainPU";

    /** Nombre de la tabla de usuarios en la base de datos */
    public static final String TABLE_USUARIO = "usuario";

    /** Nombre de la tabla de roles en la base de datos */
    public static final String TABLE_ROL = "rol";

    /** Nombre de la tabla de relación usuario-rol en la base de datos */
    public static final String TABLE_USUARIO_ROL = "usuario_rol";

}
