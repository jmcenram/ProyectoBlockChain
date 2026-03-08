package es.jmcenram.blockchain.config;

public final class AppConstants {

    private AppConstants() {
        // Evita instanciación
    }

    public static final String PERSISTENCE_UNIT = "blockchainPU";

    public static final String TABLE_USUARIO = "usuario";
    public static final String TABLE_ROL = "rol";
    public static final String TABLE_USUARIO_ROL = "usuario_rol";

}
