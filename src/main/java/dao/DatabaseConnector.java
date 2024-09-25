package dao;

import exceptions.DatabaseCloseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector implements AutoCloseable {
    private final Connection connection;

    public DatabaseConnector(String url, String user, String pass) throws SQLException {
        this.connection = DriverManager.getConnection(url, user, pass);
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws Exception {
        try {
            connection.close();
        } catch (RuntimeException e) {
            if (!connection.isClosed()) {
                throw new DatabaseCloseException(e.getMessage());
            }
        }
    }
}
