package server;

import com.google.gson.JsonSyntaxException;
import command.Command;
import command.CommandDeserializer;
import logger.DefaultFileHandler;
import manager.Manager;

import java.util.logging.Level;
import java.util.logging.Logger;

import static messages.UserMessages.*;
import static messages.ResultMessages.*;
import static messages.ServerMessages.*;

public class RequestHandler {
    private final Manager manager;
    private final String AUTOSAVE_PATH = "AUTOSAVE.json";
    private final CommandDeserializer deserializer;
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Manager manager) {
        this.manager = manager;
        this.deserializer = new CommandDeserializer();
        logger.addHandler(DefaultFileHandler.getFileHandler());
        logger.setUseParentHandlers(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> manager.save(AUTOSAVE_PATH)));
    }

    public Command handleRequest(String request) {
        logger.info(START_DESERIALIZATION);
        try {
            return deserializer.deserialize(request);
        } catch (JsonSyntaxException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
    }

    public String getHandlerResult(Command command) {
        if (command == null) {
            return UNEXPECTED_ERROR;
        }
        logger.info(DESERIALIZATION_COMPLETED);
        logger.info(COMMAND_EXECUTION);
        logger.info(command.toString());
        try {
            if (hasTwoArg(command)) {
                if (command.getName().equals("update")) {
                    return manager.updateById(deserializer.readMusicBand(command), Integer.parseInt(command.getSecondArg()));
                }
                return NO_SUCH_COMMAND;
            } else if (hasOneArg(command)) {
                switch (command.getName()) {
                    case "add" -> {
                        return manager.add(deserializer.readMusicBand(command));
                    }
                    case "add_if_min" -> {
                        return manager.addIfMin(deserializer.readMusicBand(command));
                    }
                    case "filter_by_best_album" -> {
                        return manager.filterByBestAlbum(deserializer.readBestAlbum(command));
                    }
                    case "remove" -> {
                        return manager.removeById(Integer.parseInt(command.getFirstArg()));
                    }
                    case "remove_lower" -> {
                        return manager.removeLower(Long.parseLong(command.getFirstArg()));
                    }
                    default -> {
                        return NO_SUCH_COMMAND;
                    }
                }
            } else {
                switch (command.getName()) {
                    case "help" -> {
                        return manager.help();
                    }
                    case "info" -> {
                        return manager.info();
                    }
                    case "show" -> {
                        return manager.show();
                    }
                    case "clear" -> {
                        return manager.clear();
                    }
                    case "history" -> {
                        return manager.history();
                    }
                    case "min_by_best_album" -> {
                        return manager.minByBestAlbum();
                    }
                    case "print_field_asc_best_album" -> {
                        return manager.printFieldAscBestAlbum();
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

    private boolean hasOneArg (Command command){
        return command.getFirstArg() != null;
    }

    private boolean hasTwoArg (Command command){
        return command.getFirstArg() != null && command.getSecondArg() != null;
    }

    public void save() {
        logger.log(Level.WARNING, manager.save(AUTOSAVE_PATH));
    }

    public String readEnvironment() {
        logger.info(READING_ENVIRONMENT);
        StringBuilder sb = new StringBuilder();
        sb.append(GREET_MESSAGE);
        String readResult;
        String env = System.getenv("SAVED_COLLECTION");
        if (env == null) {
            readResult = WORK_WITH_EMPTY_COLLECTION;
            sb.append(WORK_WITH_EMPTY_COLLECTION);
        } else {
            readResult = manager.read(env);
            sb.append(readResult);
        }
        logger.info(readResult);
        return sb.toString();
    }
}
