package dao;

import exceptions.NoSuchUserException;
import logger.LoggerConfigurator;
import user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static messages.ExceptionsDAOMessages.AFTER_ROLLBACK;
import static messages.ExceptionsDAOMessages.BEFORE_ROLLBACK;

public class UserDAO {
    private final DatabaseConnector connector;
    private static final Logger logger = LoggerConfigurator.createDefaultLogger(UserDAO.class.getName());

    public UserDAO(DatabaseConnector connector) {
        this.connector = connector;
    }

    public User selectUserByLogin(String login) throws SQLException {
        try (Connection connection = connector.getConnection()) {
            return selectUserHelper(login, connection);
        } catch (SQLException e) {
            String message = String.format("Login: %s, Exception: %s", login, e.getMessage());
            logger.log(Level.SEVERE, message);
            throw e;
        }
    }

    private User selectUserHelper(String login, Connection connection)
            throws SQLException, NoSuchUserException {
        String sql = "SELECT * FROM owners WHERE login = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int argCount = 0;
            preparedStatement.setString(++argCount, login);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("owner_id");
                String pass = resultSet.getString("pass");
                return new User(id, login, pass);
            } else {
                throw new NoSuchUserException();
            }
        }
    }

    public boolean regUser(String login, String encodePass) throws SQLException {
        try (Connection connection = connector.getConnection()) {
            try {
                connection.setAutoCommit(false);
                selectUserHelper(login, connection);
                return false;

            } catch (SQLException e) {
                String messageBefore = String.format(BEFORE_ROLLBACK + "Login: %s, Exception: %s", login, e.getMessage());
                logger.log(Level.SEVERE, messageBefore);
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    String messageAfter = String.format(AFTER_ROLLBACK + "Login: %s, Exception: %s", login, e.getMessage());
                    logger.log(Level.SEVERE, messageAfter);
                    throw ex;
                }
                throw e;
            } catch (NoSuchUserException e) {
                String sql = "INSERT INTO owners (login, pass) VALUES (?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, login);
                    preparedStatement.setString(2, encodePass);
                    connection.commit();

                    return preparedStatement.executeUpdate() != 0;
                }
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
