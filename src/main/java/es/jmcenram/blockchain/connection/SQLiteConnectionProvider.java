package es.jmcenram.blockchain.connection;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteConnectionProvider implements ConnectionProvider {

    @Override
    public Connection getConnection() throws SQLException {
        return SQLiteConnection.openConnection();
    }

    @Override
    public void closeConnection(Connection conn) throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return ConnectionProvider.class.equals(unwrapType)
                || SQLiteConnectionProvider.class.isAssignableFrom(unwrapType);
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        if (isUnwrappableAs(unwrapType)) {
            return unwrapType.cast(this);
        }
        throw new UnknownUnwrapTypeException(unwrapType);
    }
}
