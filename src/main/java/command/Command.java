package command;

public class Command {
    private final String name;
    private final String firstArg;
    private final String secondArg;

    public Command(String name) {
        this.name = name;
        this.firstArg = null;
        this.secondArg = null;
    }

    public String getName() {
        return name;
    }

    public String getFirstArg() {
        return firstArg;
    }

    public String getSecondArg() {
        return secondArg;
    }

    @Override
    public String toString() {
        return "Command{" +
                "name='" + name + '\'' +
                ", firstArg='" + firstArg + '\'' +
                ", secondArg='" + secondArg + '\'' +
                '}' + '\n';
    }
}
