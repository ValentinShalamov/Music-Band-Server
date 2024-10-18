package exceptions;

public class DatabaseCloseException extends RuntimeException {
    public DatabaseCloseException(String message) {
        super(message);
    }
}
