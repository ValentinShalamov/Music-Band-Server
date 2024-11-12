package manager;

import dao.UserDAO;
import exceptions.NoSuchUserException;
import org.apache.commons.codec.digest.DigestUtils;
import user.User;

import java.sql.SQLException;

import static messages.ErrorMessages.SQL_EXCEPTION;
import static messages.UserMessages.REGISTRATION_SUCCESSFUL;
import static messages.UserMessages.USER_EXISTS_ALREADY;

public class LoginAndRegisterManager {
    private final UserDAO userDAO;


    public LoginAndRegisterManager(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User getUser(String login, String pass) throws SQLException, NoSuchUserException {
        User user = userDAO.selectUserByLogin(login);
        if (user.pass().equals(encodePass(pass))) {
            return user;
        } else {
            throw new NoSuchUserException();
        }
    }

    public String regUser(String login, String pass) {
        try {
            if (userDAO.regUser(login, encodePass(pass))) {
                return REGISTRATION_SUCCESSFUL;
            } else {
                return USER_EXISTS_ALREADY;
            }
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        }
    }

    private String encodePass(String pass) {
        return DigestUtils.sha256Hex(pass);
    }
}
