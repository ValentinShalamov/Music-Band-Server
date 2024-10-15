package manager;

import dao.MusicBandDAO;
import exceptions.DatabaseValidationException;
import exceptions.MusicBandExistsException;
import music.MusicBand;
import user.User;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static messages.ErrorMessages.SQL_EXCEPTION;
import static messages.HistoryMessages.*;
import static messages.ResultMessages.*;

public class MusicBandManager {
    private final CacheManager cacheManager;
    private final MusicBandDAO musicBandDAO;
    private final Map<String, Deque<String>> history = new HashMap<>();

    public MusicBandManager(CacheManager cacheManager, MusicBandDAO musicBandDAO) throws SQLException {
        this.cacheManager = cacheManager;
        this.musicBandDAO = musicBandDAO;
        initCache();
    }

    public String help(User user) {
        addHistory(HELP_HISTORY, user);
        return cacheManager.help();
    }

    public String info(User user) {
        addHistory(INFO_HISTORY, user);
        return cacheManager.info();
    }

    public String show(User user) {
        addHistory(SHOW_HISTORY, user);
        return cacheManager.show();
    }

    public String showMine(User user) {
        addHistory(SHOW_MINE_HISTORY, user);
        return cacheManager.showMine(user);
    }

    public String add(MusicBand musicBand, User user) {
        addHistory(ADD_HISTORY, user);
        try {
            cacheManager.add(musicBandDAO.insertBandAndSelect(musicBand, user));
            return MUSIC_BAND_HAS_BEEN_ADDED_SUCCESSFUL;
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        } catch (MusicBandExistsException e) {
            return ADDITION_MISTAKE;
        }
    }

    public String updateById(MusicBand musicBand, long id, User user) {
        addHistory(UPDATE_ID_HISTORY, user);
        try {
            if (musicBandDAO.updateById(musicBand, id, user)) {
                musicBand.setUser(user);
                musicBand.setId(id);
                cacheManager.updateById(musicBand, id);
                return MUSIC_BAND_HAS_BEEN_UPDATED_SUCCESSFUL;
            } else {
                return NO_SUCH_ID;
            }
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        }
    }

    public String removeById(long id, User user) {
        addHistory(REMOVE_ID_HISTORY, user);
        try {
            if (musicBandDAO.removeById(id, user)) {
                cacheManager.removeById(id);
                return MUSIC_BAND_HAS_BEEN_DELETED_SUCCESSFUL;
            } else {
                return DELETED_MISTAKE;
            }
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        }
    }

    public String clear(User user) {
        addHistory(CLEAR_HISTORY, user);
        try {
            if (musicBandDAO.clear(user)) {
                cacheManager.clear(user);
                return COLLECTION_HAS_BEEN_DELETED;
            } else {
                return COLLECTION_IS_EMPTY;
            }
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        }
    }

    public String history(User user) {
        String login = user.login();
        if (history.get(login).isEmpty()) {
            return HISTORY_IS_EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        String firstCommand = history.get(login).getFirst();
        builder.append(firstCommand).delete(firstCommand.length() - 2, firstCommand.length()).append(" - last command \n");
        history.get(login).stream()
                .skip(1)
                .forEach(builder::append);
        return builder.toString();
    }

    public String clearHistory(User user) {
        String login = user.login();
        if (history.get(login).isEmpty()) {
            return HISTORY_IS_EMPTY;
        }
        history.get(user.login()).clear();
        return HISTORY_CLEARED;
    }

    public String addIfMin(MusicBand musicBand, User user) {
        addHistory(ADD_IF_MIN_HISTORY, user);
        try {
            cacheManager.add(musicBandDAO.insertIfSalesMin(musicBand, user));
            return MUSIC_BAND_HAS_BEEN_ADDED_SUCCESSFUL;
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        } catch (MusicBandExistsException e) {
            return ADDITION_MISTAKE;
        } catch (DatabaseValidationException e) {
            return SALES_BIGGER_THAN_MIN_ELEMENT;
        }
    }

    public String removeLower(long sales, User user) {
        addHistory(REMOVE_LOWER_HISTORY, user);
        try {
            musicBandDAO.removeLower(sales, user);
            return cacheManager.removeLower(sales, user);
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        }
    }

    public String minByBestAlbum(User user) {
        addHistory(MIN_BY_BEST_ALBUM_HISTORY, user);
        return cacheManager.minByBestAlbum();
    }

    public String filterByBestAlbum(long sales, User user) {
        addHistory(FILTER_BY_BEST_ALBUM_HISTORY, user);
        return cacheManager.filterByBestAlbum(sales);
    }

    public String printFieldAscBestAlbum(User user) {
        addHistory(PRINT_FIELD_ASC_BEST_ALBUM_HISTORY, user);
        return cacheManager.printFieldAscBestAlbum();
    }

    public void initCommandHistory(User user) {
        history.putIfAbsent(user.login(), new ArrayDeque<>());
    }

    private void initCache() throws SQLException {
        cacheManager.initBands(musicBandDAO.readMusicBands());
    }

    private void addHistory(String commandName, User user) {
        Deque<String> commandHistory = history.get(user.login());
        commandHistory.addFirst(commandName);
        if (commandHistory.size() > 12) {
            commandHistory.removeLast();
        }
    }
}
