package dao;

import exceptions.DatabaseCloseException;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnector implements AutoCloseable {
    private final BasicDataSource dataSource = new BasicDataSource();

    public DatabaseConnector(String url, String user, String pass) {
        this.dataSource.setUrl(url);
        this.dataSource.setUsername(user);
        this.dataSource.setPassword(pass);

        this.dataSource.setDefaultAutoCommit(true);
        this.dataSource.setMinIdle(5); // minimum number of connection objects that have kept alive in the pool
        this.dataSource.setMaxIdle(10); // maximum number of connection objects that have kept alive in the pool
        this.dataSource.setMaxOpenPreparedStatements(100);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() throws Exception {
        try {
            dataSource.close();
        } catch (RuntimeException e) {
            if (!dataSource.isClosed()) {
                throw new DatabaseCloseException(e.getMessage());
            }
        }
    }
}
