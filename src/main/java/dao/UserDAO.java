package dao;

import exceptions.NoSuchUserException;
import org.apache.commons.codec.digest.DigestUtils;
import user.User;

import java.sql.*;

public class UserDAO {
    public DatabaseConnector connector;
    public Connection connection;

    public UserDAO(DatabaseConnector connector) {
        this.connector = connector;
        this.connection = connector.getConnection();
    }

    public User selectUser(String login, String pass) throws SQLException {
        String sql = "SELECT * FROM owners WHERE login = ? AND pass = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, encodePass(pass));

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("owner_id");
                return new User(id, login);
            } else {
                throw new NoSuchUserException();
            }
        }
    }

    private boolean hasUserByLogin(String login) throws SQLException {
        String sql = "SELECT * FROM owners WHERE login = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, login);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    public boolean regUser(String login, String pass) throws SQLException {
        if (!hasUserByLogin(login)) {
            String sql = "INSERT INTO owners (login, pass) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, login);
                preparedStatement.setString(2, encodePass(pass));

                return preparedStatement.executeUpdate() != 0;
            }
        } else {
            return false;
        }
    }

    private String encodePass(String pass) {
        return DigestUtils.sha256Hex(pass);
    }

}
