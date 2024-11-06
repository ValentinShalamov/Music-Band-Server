package exceptions;

public class ServerCloseException extends RuntimeException {
    public ServerCloseException(String message) {
        super(message);
    }
}
