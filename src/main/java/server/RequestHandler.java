package server;

import com.google.gson.JsonSyntaxException;
import command.Command;
import command.CommandDeserializer;
import manager.Manager;

import static messages.UserMessages.*;
import static messages.ResultMessages.*;

public class RequestHandler {
    private final Manager manager;
    private final String AUTOSAVE_PATH = "AUTOSAVE.json";
    private final CommandDeserializer deserializer;

    public RequestHandler(Manager manager) {
        this.manager = manager;
        this.deserializer = new CommandDeserializer();
    }

    public Command handleRequest(String request) {
        return deserializer.deserialize(request);
    }

    public String getHandlerResult(Command command) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> manager.save(AUTOSAVE_PATH)));
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

    public String save() {
        return manager.save(AUTOSAVE_PATH);
    }

    public String readEnvironment() {
        StringBuilder sb = new StringBuilder();
        sb.append(GREET_MESSAGE);
        String env = System.getenv("SAVED_COLLECTION");
        if (env == null) {
            sb.append(WORK_WITH_EMPTY_COLLECTION);
        } else {
            sb.append(manager.read(env));
        }
        return sb.toString();
    }
}
