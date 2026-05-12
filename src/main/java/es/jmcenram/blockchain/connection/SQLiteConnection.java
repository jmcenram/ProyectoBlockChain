package es.jmcenram.blockchain.connection;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class SQLiteConnection {

    private static final String APP_NAME = "BlockchainApp";
    private static final String DB_NAME = "database.db";

    private SQLiteConnection() {
    }

    public static Connection connect() {
        try {
            return openConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Error conectando con SQLite", e);
        }
    }

    static Connection openConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");

            Path dbPath = getDatabasePath();
            copyDatabaseIfMissing(dbPath);

            Connection conn = DriverManager.getConnection(getJdbcUrl(dbPath));
            enableForeignKeys(conn);
            initializeBaseData(conn);

            return conn;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Error conectando con SQLite", e);
        }
    }

    public static Path getDatabasePath() {
        String appData = System.getenv("APPDATA");
        if (appData == null || appData.isBlank()) {
            appData = System.getProperty("user.home");
        }

        return Path.of(appData, APP_NAME, DB_NAME);
    }

    public static String getJdbcUrl() {
        return getJdbcUrl(getDatabasePath());
    }

    private static String getJdbcUrl(Path dbPath) {
        return "jdbc:sqlite:" + dbPath.toAbsolutePath();
    }

    private static void copyDatabaseIfMissing(Path dbPath) throws Exception {
        Files.createDirectories(dbPath.getParent());

        if (Files.exists(dbPath)) {
            return;
        }

        try (InputStream is = SQLiteConnection.class.getResourceAsStream("/db/" + DB_NAME)) {
            if (is == null) {
                throw new IllegalStateException("No se encontro la base de datos en resources/db/" + DB_NAME);
            }

            Files.copy(is, dbPath);
        }
    }

    private static void enableForeignKeys(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
    }

    private static void initializeBaseData(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.executeUpdate("""
                    INSERT OR IGNORE INTO rol (nombre, descripcion, fecha_creacion)
                    VALUES ('MASTER', 'Responsable de configuracion blockchain y entidades emisoras', CURRENT_TIMESTAMP)
                    """);
            statement.executeUpdate("""
                    INSERT OR IGNORE INTO rol (nombre, descripcion, fecha_creacion)
                    VALUES ('ADMIN', 'Administrador funcional del sistema', CURRENT_TIMESTAMP)
                    """);
            statement.executeUpdate("""
                    INSERT OR IGNORE INTO rol (nombre, descripcion, fecha_creacion)
                    VALUES ('USER', 'Usuario basico de la aplicacion', CURRENT_TIMESTAMP)
                    """);
            statement.executeUpdate("""
                    INSERT OR IGNORE INTO usuario_rol (usuario_id, rol_id, fecha_creacion)
                    SELECT u.id, r.id, CURRENT_TIMESTAMP
                    FROM usuario u
                    JOIN rol r ON UPPER(r.nombre) = 'MASTER'
                    WHERE u.id = (SELECT MIN(id) FROM usuario)
                      AND NOT EXISTS (SELECT 1 FROM usuario_rol)
                    """);
            statement.executeUpdate("""
                    INSERT OR IGNORE INTO usuario_rol (usuario_id, rol_id, fecha_creacion)
                    SELECT u.id, r.id, CURRENT_TIMESTAMP
                    FROM usuario u
                    JOIN rol r ON UPPER(r.nombre) =
                        CASE
                            WHEN u.entidad_emisora_id IS NOT NULL THEN 'ADMIN'
                            ELSE 'USER'
                        END
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM usuario_rol ur
                        WHERE ur.usuario_id = u.id
                    )
                    """);
        }
    }
}
