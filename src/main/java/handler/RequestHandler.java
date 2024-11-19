package handler;

import com.google.gson.JsonSyntaxException;
import command.Command;
import command.CommandDeserializer;
import exceptions.NoSuchUserException;
import exceptions.UserExistsException;
import logger.LoggerConfigurator;
import manager.LoginAndRegisterManager;
import manager.MusicBandManager;
import user.User;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static messages.ErrorMessages.DESERIALIZATION_ERROR;
import static messages.ErrorMessages.SQL_EXCEPTION;
import static messages.ResultMessages.INCORRECT_ARGUMENT;
import static messages.ResultMessages.NO_SUCH_COMMAND;
import static messages.ServerMessages.*;
import static messages.UserMessages.*;

public class RequestHandler {
    private final MusicBandManager musicBandManager;
    private final CommandDeserializer deserializer;
    private final LoginAndRegisterManager loginAndRegisterManager;

    private static final Logger logger = LoggerConfigurator.createDefaultLogger(RequestHandler.class.getName());

    public RequestHandler(MusicBandManager musicBandManager, LoginAndRegisterManager loginAndRegisterManager) {
        this.musicBandManager = musicBandManager;
        this.loginAndRegisterManager = loginAndRegisterManager;
        this.deserializer = new CommandDeserializer();
    }

    public String getHandleRequestResult(String request, UserContext context) {
        return handleCommand(deserializeRequest(request), context);
    }

    private Command deserializeRequest(String request) {
        logger.info(START_DESERIALIZATION);
        try {
            return deserializer.deserialize(request);
        } catch (JsonSyntaxException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }

    private String logIn(String login, String pass, UserContext context) {
        try {
            User user = loginAndRegisterManager.getUser(login, pass);
            context.setUser(user);
            musicBandManager.initCommandHistory(user);
            return String.format("%sUsername: %s \n", AUTHORIZATION_SUCCESSFUL, user.login());
        } catch (NoSuchUserException e) {
            return USER_NOT_FOUND;
        } catch (SQLException e) {
            return SQL_EXCEPTION;
        } catch (UserExistsException e) {
            return USER_IS_AUTHORIZED;
        }
    }

    private String handleCommand(Command command, UserContext context) {
        if (command == null) {
            return DESERIALIZATION_ERROR;
        }
        User user = context.getUser();
        if (user == null && (!command.getName().equals("login") && !command.getName().equals("register"))) {
            return AUTHORIZATION_ERROR;
        }
        if (user != null && (command.getName().equals("login") || command.getName().equals("register"))) {
            return AUTHENTICATION_ALREADY_EXECUTED;
        }
        logger.info(DESERIALIZATION_COMPLETED);
        logger.info(COMMAND_EXECUTION);
        logger.info(command.toString());
        try {
            if (hasTwoArg(command)) {
                if (command.getName().equals("update")) {
                    return musicBandManager.updateById(deserializer.readMusicBand(command), Integer.parseInt(command.getSecondArg()), user);
                } else if (command.getName().equals("login")) {
                    return logIn(command.getFirstArg(), command.getSecondArg(), context);
                } else if (command.getName().equals("register")) {
                    String result = loginAndRegisterManager.regUser(command.getFirstArg(), command.getSecondArg());
                    if (result.equals(REGISTRATION_SUCCESSFUL)) return logIn(command.getFirstArg(), command.getSecondArg(), context);
                    return result;
                }
                return NO_SUCH_COMMAND;
            } else if (hasOneArg(command)) {
                switch (command.getName()) {
                    case "add" -> {
                        return musicBandManager.add(deserializer.readMusicBand(command), user);
                    }
                    case "add_if_min" -> {
                        return musicBandManager.addIfMin(deserializer.readMusicBand(command), user);
                    }
                    case "filter" -> {
                        return musicBandManager.filter(Long.parseLong(command.getFirstArg()), user);
                    }
                    case "remove" -> {
                        return musicBandManager.removeById(Integer.parseInt(command.getFirstArg()), user);
                    }
                    case "remove_lower" -> {
                        return musicBandManager.removeLower(Long.parseLong(command.getFirstArg()), user);
                    }
                    default -> {
                        return NO_SUCH_COMMAND;
                    }
                }
            } else {
                switch (command.getName()) {
                    case "help" -> {
                        return musicBandManager.help(user);
                    }
                    case "info" -> {
                        return musicBandManager.info(user);
                    }
                    case "show" -> {
                        return musicBandManager.show(user);
                    }
                    case "show_mine" -> {
                        return musicBandManager.showMine(user);
                    }
                    case "show_min" -> {
                        return musicBandManager.showMin(user);
                    }
                    case "clear" -> {
                        return musicBandManager.clear(user);
                    }
                    case "history" -> {
                        return musicBandManager.history(user);
                    }
                    case "history_clear" -> {
                        return musicBandManager.clearHistory(user);
                    }
                    case "print_asc" -> {
                        return musicBandManager.printAsc(user);
                    }
                    case "print_desc" -> {
                        return musicBandManager.printDesc(user);
                    }
                    default -> {
                        return NO_SUCH_COMMAND;
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            return INCORRECT_ARGUMENT;
        }
    }

    private boolean hasOneArg(Command command) {
        return command.getFirstArg() != null;
    }

    private boolean hasTwoArg(Command command) {
        return command.getFirstArg() != null && command.getSecondArg() != null;
    }
}
