package multithreading;

import handler.RequestHandler;
import handler.UserContext;
import logger.LoggerConfigurator;
import response.Response;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static messages.ErrorMessages.BLOCK_QUEUE_PUT_ERROR;
import static messages.ServerMessages.PROCESSING_REQUEST;

public class MultithreadedRequestHandler {
    private final ExecutorService threadPoll;
    private final BlockingQueue<Response> responses;
    private final RequestHandler requestHandler;
    private final Logger logger = LoggerConfigurator.createDefaultLogger(MultithreadedRequestHandler.class.getName());

    public MultithreadedRequestHandler(ExecutorService threadPoll, BlockingQueue<Response> responses, RequestHandler requestHandler) {
        this.threadPoll = threadPoll;
        this.responses = responses;
        this.requestHandler = requestHandler;
    }

    public void execute(int requestId, String request, UserContext userContext) {
        threadPoll.execute(() -> {
            logger.info(PROCESSING_REQUEST);
            try {
                String response = requestHandler.getHandleRequestResult(request, userContext);
                responses.put(new Response(requestId, response));
            } catch (InterruptedException e) {
                String message = String.format("Thread name: %s, message: %s", Thread.currentThread().getName(), BLOCK_QUEUE_PUT_ERROR);
                logger.log(Level.SEVERE, message, e);
            }
        });
    }
}
