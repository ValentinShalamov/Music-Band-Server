package manager;

import exceptions.DatabaseValidationException;
import exceptions.MusicBandExistsException;
import exceptions.NoSuchUserException;
import music.MusicBand;
import server.MessageReadingContext;
import user.User;

import java.sql.SQLException;
import java.util.ArrayDeque;

import static messages.ErrorMessages.SQL_EXCEPTION;
import static messages.ResultMessages.*;
import static messages.UserMessages.*;

public class Manager {
    private final CashManager cashManager;
    private final ManagerDAO managerDAO;

    public Manager(CashManager cashManager, ManagerDAO managerDAO) {
        this.cashManager = cashManager;
        this.managerDAO = managerDAO;
    }

    public String initUser(String login, String pass, MessageReadingContext context) {
        try {
            User user = managerDAO.getUser(login, pass);
            context.setUser(user);
            return AUTHORIZATION_SUCCESSFUL;
        } catch (NoSuchUserException e) {
            return NO_SUCH_USER;
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        }

    }

    public String regUser(String login, String pass) {
        try {
            if (managerDAO.regUser(login, pass)) {
                return REGISTRATION_SUCCESSFUL;
            } else {
                return USER_EXISTS_ALREADY;
            }
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        }
    }

    public String help(User user) {
        user.addHistory(" - help \n");
        return cashManager.help();
    }

    public String info(User user) {
        user.addHistory(" - info \n");
        return cashManager.info();
    }

    public String show(User user) {
        user.addHistory(" - show \n");
        return cashManager.show();
    }

    public String showMine(User user) {
        user.addHistory(" - show_mine \n");
        return cashManager.showMine(user.getId());
    }

    public String add(MusicBand musicBand, User user) {
        user.addHistory(" - add \n");
        try {
            cashManager.add(managerDAO.addAndGet(musicBand, user));
            return MUSIC_BAND_HAS_BEEN_ADDED_SUCCESSFUL;
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        } catch (MusicBandExistsException e) {
            return ADDITION_MISTAKE;
        }
    }

    public String updateById(MusicBand musicBand, long id, User user) {
        user.addHistory(" - update <id> \n");
        try {
            if (managerDAO.updateById(musicBand, id, user)) {
                musicBand.setUser(user);
                musicBand.setId(id);
                cashManager.updateById(musicBand, id);
                return MUSIC_BAND_HAS_BEEN_UPDATED_SUCCESSFUL;
            } else {
                return NO_SUCH_ID;
            }
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        }
    }

    public String removeById(long id, User user) {
        user.addHistory(" - remove <id> \n");
        try {
            if (managerDAO.removeById(id, user)) {
                cashManager.removeById(id);
                return MUSIC_BAND_HAS_BEEN_DELETED_SUCCESSFUL;
            } else {
                return DELETED_MISTAKE;
            }
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        }
    }

    public String clear(User user) {
        user.addHistory(" - clear \n");
        try {
            if (managerDAO.clear(user)) {
                cashManager.clear(user.getId());
                return COLLECTION_HAS_BEEN_DELETED;
            } else {
                return COLLECTION_IS_EMPTY;
            }
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        }
    }

    public String history(User user) {
        ArrayDeque<String> history = user.getHistoryCommand();
        if (history.isEmpty()) {
            return HISTORY_IS_EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        String firstCommand = history.getFirst();
        builder.append(firstCommand).delete(firstCommand.length() - 2, firstCommand.length()).append(" - last command \n");
        history.stream()
                .skip(1)
                .forEach(builder::append);
        return builder.toString();
    }

    public String addIfMin(MusicBand musicBand, User user) {
        try {
            user.addHistory(" - add_if_min \n");
            cashManager.add(managerDAO.addIfMin(musicBand, user));
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
        user.addHistory("- remove_lower \n");
        try {
            managerDAO.removeLower(sales, user);
            return cashManager.removeLower(sales, user.getId());
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        }
    }

    public String minByBestAlbum(User user) {
        user.addHistory(" - min_by_best_album \n");
        return cashManager.minByBestAlbum();
    }

    public String filterByBestAlbum(long sales, User user) {
        user.addHistory("- filter_by_sales \n");
        return cashManager.filterByBestAlbum(sales);
    }

    public String printFieldAscBestAlbum(User user) {
        user.addHistory(" - print_field_asc_best_album \n");
        return cashManager.printFieldAscBestAlbum();
    }

    public void readBands() throws SQLException {
        cashManager.initBands(managerDAO.readMusicBands());
    }
}
