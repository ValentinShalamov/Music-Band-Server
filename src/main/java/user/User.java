package user;

import java.util.ArrayDeque;

public class User {
    private final int id;
    private final String login;
    private final ArrayDeque<String> historyCommand = new ArrayDeque<>();

    public User(int id, String login) {
        this.id = id;
        this.login = login;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void addHistory(String commandName) {
        historyCommand.addFirst(commandName);
        if (historyCommand.size() > 12) {
            historyCommand.removeLast();
        }
    }

    public ArrayDeque<String> getHistoryCommand() {
        return historyCommand.clone();
    }
}
