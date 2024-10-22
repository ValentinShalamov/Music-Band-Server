package messages;

public class ErrorMessages {
    public static final String UNEXPECTED_ERROR = "Unexpected error \n";
    public static final String DESERIALIZATION_ERROR = "The your client is unsupported \n";
    public static final String UNSUPPORTED_CLIENT = "The message has sent from unsupported client \n";
    public static final String CHANNEL_FAILURE = "Channel failure \n";
    public static final String SQL_EXCEPTION = "Server error. Try the query again \n";
    public static final String SERVER_CLOSE_ERROR = "The server did not close the channel \n";
    public static final String BLOCK_QUEUE_PUT_ERROR = "The thread has not succeeded in adding a response to the blocking queue \n";
    public static final String BLOCK_QUEUE_TAKE_ERROR = "The thread has not got a task from blocking queue \n";
}
