package manager;

import dao.MusicBandDAO;
import dao.UserDAO;
import music.MusicBand;
import user.User;

import java.sql.SQLException;
import java.util.Set;

public class ManagerDAO {
    private final MusicBandDAO musicBandDAO;
    private final UserDAO userDAO;

    public ManagerDAO(MusicBandDAO musicBandDAO, UserDAO userDAO) {
        this.musicBandDAO = musicBandDAO;
        this.userDAO = userDAO;
    }

    public User getUser(String login, String pass) throws SQLException {
        return userDAO.selectUser(login, pass);
    }

    public boolean regUser(String login, String pass) throws SQLException {
        return userDAO.regUser(login, pass);
    }

    public MusicBand addAndGet(MusicBand musicBand, User user) throws SQLException {
        return musicBandDAO.insertBandAndSelect(musicBand, user);
    }

    public boolean updateById(MusicBand musicBand, long id, User user) throws SQLException {
        return musicBandDAO.updateById(musicBand, id, user);
    }

    public boolean removeById(long id, User user) throws SQLException {
        return musicBandDAO.removeById(id, user);
    }

    public boolean clear(User user) throws SQLException {
        return musicBandDAO.clear(user);
    }

    public MusicBand addIfMin(MusicBand musicBand, User user) throws SQLException {
        return musicBandDAO.insertIfSalesMin(musicBand, user);
    }

    public void removeLower(long sales, User user) throws SQLException {
        musicBandDAO.removeLower(sales, user);
    }

    public Set<MusicBand> readMusicBands() throws SQLException {
        return musicBandDAO.readMusicBands();
    }

}
