package server;

import com.google.gson.JsonSyntaxException;
import command.Command;
import command.CommandDeserializer;
import logger.LoggerConfigurator;
import manager.Manager;
import user.User;

import java.util.logging.Level;
import java.util.logging.Logger;

import static messages.ErrorMessages.DESERIALIZATION_ERROR;
import static messages.ResultMessages.INCORRECT_ARGUMENT;
import static messages.ResultMessages.NO_SUCH_COMMAND;
import static messages.ServerMessages.*;
import static messages.UserMessages.*;

public class RequestHandler {
    private final Manager manager;
    private final CommandDeserializer deserializer;
    private static final Logger logger = LoggerConfigurator.createDefaultLogger(RequestHandler.class.getName());

    public RequestHandler(Manager manager) {
        this.manager = manager;
        this.deserializer = new CommandDeserializer();
    }

    public String getHandleRequestResult(String request, MessageReadingContext context) {
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

    private String handleCommand(Command command, MessageReadingContext context) {
        if (command == null) {
            return DESERIALIZATION_ERROR;
        }
        User user = context.getUser();
        if (user == null && (!command.getName().equals("log") && !command.getName().equals("reg"))) {
            return AUTHORIZATION_ERROR;
        }
        if (user != null && (command.getName().equals("log") || command.getName().equals("reg"))) {
            return AUTHENTICATION_ALREADY_EXECUTED;
        }
        logger.info(DESERIALIZATION_COMPLETED);
        logger.info(COMMAND_EXECUTION);
        logger.info(command.toString());
        try {
            if (hasTwoArg(command)) {
                if (command.getName().equals("update")) {
                    return manager.updateById(deserializer.readMusicBand(command), Integer.parseInt(command.getSecondArg()), user);
                } else if (command.getName().equals("log")) {
                    return manager.initUser(command.getFirstArg(), command.getSecondArg(), context);
                } else if (command.getName().equals("reg")) {
                    return manager.regUser(command.getFirstArg(), command.getSecondArg());
                }
                return NO_SUCH_COMMAND;
            } else if (hasOneArg(command)) {
                switch (command.getName()) {
                    case "add" -> {
                        return manager.add(deserializer.readMusicBand(command), user);
                    }
                    case "add_if_min" -> {
                        return manager.addIfMin(deserializer.readMusicBand(command), user);
                    }
                    case "filter_by_best_album" -> {
                        return manager.filterByBestAlbum(Long.parseLong(command.getFirstArg()), user);
                    }
                    case "remove" -> {
                        return manager.removeById(Integer.parseInt(command.getFirstArg()), user);
                    }
                    case "remove_lower" -> {
                        return manager.removeLower(Long.parseLong(command.getFirstArg()), user);
                    }
                    default -> {
                        return NO_SUCH_COMMAND;
                    }
                }
            } else {
                switch (command.getName()) {
                    case "help" -> {
                        return manager.help(user);
                    }
                    case "info" -> {
                        return manager.info(user);
                    }
                    case "show" -> {
                        return manager.show(user);
                    }
                    case "show_mine" -> {
                        return manager.showMine(user);
                    }
                    case "clear" -> {
                        return manager.clear(user);
                    }
                    case "history" -> {
                        return manager.history(user);
                    }
                    case "min_by_best_album" -> {
                        return manager.minByBestAlbum(user);
                    }
                    case "print_field_asc_best_album" -> {
                        return manager.printFieldAscBestAlbum(user);
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

    public String readGreetMessage() {
        return GREET_MESSAGE;
    }
}
