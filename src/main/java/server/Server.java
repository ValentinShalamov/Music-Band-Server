package server;

import exceptions.ClientDisconnectedException;
import exceptions.NotFullMessageException;
import exceptions.ServerCloseException;
import exceptions.UnsupportedClientException;
import handler.MessageReader;
import handler.RequestHandler;
import handler.UserContext;
import logger.LoggerConfigurator;
import response.Response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static messages.ErrorMessages.*;
import static messages.ServerMessages.*;

public class Server implements AutoCloseable {
    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final RequestHandler requestHandler;
    private final MessageReader messageReader;
    private final CurrentUsers currentUsers;
    private final CurrentRequests currentRequests;
    private final ExecutorService handleService;
    private final BlockingQueue<Response> responses;
    private static final Logger logger = LoggerConfigurator.createDefaultLogger(Server.class.getName());


    public Server(RequestHandler requestHandler, String address, int port) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(address, port));
        this.serverSocketChannel.configureBlocking(false);
        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.requestHandler = requestHandler;
        this.messageReader = new MessageReader();
        this.currentUsers = new CurrentUsers();
        this.currentRequests = new CurrentRequests();
        this.responses = new ArrayBlockingQueue<>(100);
        this.handleService = Executors.newFixedThreadPool(10);
        initSender();
    }

    private void initSender() {
        Thread sender = new Thread(() -> {
            while (true) {
                try {
                    Response curr = responses.take();
                    logger.info(PREPARING_TO_SEND);
                    int id = curr.requestId();
                    String result = curr.result();
                    SocketChannel client = currentRequests.takeClient(id);
                    client.write(getMessageForSend(result));
                    logger.info(SENDING_ANSWER_TO_CLIENT);
                    if (result.equals(DESERIALIZATION_ERROR)) {
                        currentUsers.deleteClient(client);
                        logger.info(DELETING_SUCCESSFUL);
                        client.close();
                        logger.info(CONNECTION_CLOSED);
                    }
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, BLOCK_QUEUE_TAKE_ERROR, e);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, CHANNEL_FAILURE, e);
                }
            }
        });
        sender.setDaemon(true);
        sender.start();
    }

    public void start() {
        logger.setUseParentHandlers(true);
        logger.info(SERVER_RUNNING);
        logger.setUseParentHandlers(false);
        Set<SelectionKey> selectedKeys;
        Iterator<SelectionKey> iter;
        SelectionKey key;
        while (true) {
            try {
                selector.select();
                selectedKeys = selector.selectedKeys();
                iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, UNEXPECTED_ERROR, e);
            }
        }
    }

    private ByteBuffer getMessageForSend(String resultMessage) {
        byte[] requestBytes = resultMessage.getBytes(StandardCharsets.UTF_8);
        byte[] intValueBox = ByteBuffer.allocate(4).putInt(requestBytes.length).array();
        byte[] message = Arrays.copyOf(intValueBox, intValueBox.length + requestBytes.length);
        System.arraycopy(requestBytes, 0, message, 4, requestBytes.length);
        return ByteBuffer.wrap(message);
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
        try {
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            currentUsers.addClient(client);
            logger.info(NEW_CLIENT_CONNECTED + client.getRemoteAddress() + "\n");
            int requestId = currentRequests.getRequestId(client);
            responses.put(new Response(requestId, requestHandler.readGreetMessage()));
        } catch (IOException e) {
            currentUsers.deleteClient(client);
            client.close();
            logger.info(CONNECTION_CLOSED);
            logger.log(Level.SEVERE, CHANNEL_FAILURE, e);
        } catch (InterruptedException e) {
            currentUsers.deleteClient(client);
            client.close();
            logger.info(CONNECTION_CLOSED);
            String message = String.format("Thread name: %s, message: %s", Thread.currentThread().getName(), BLOCK_QUEUE_PUT_ERROR);
            logger.log(Level.SEVERE, message, e);
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        try {
            String request;
            try {
                logger.info(GETTING_REQUEST_FROM_CLIENT);
                request = messageReader.getFullMessage(client);
            } catch (NotFullMessageException e) {
                return;
            } catch (ClientDisconnectedException e) {
                currentUsers.deleteClient(client);
                client.close();
                logger.info(CONNECTION_CLOSED);
                logger.info(CLIENT_HAS_DISCONNECTED);
                return;
            } catch (UnsupportedClientException e) {
                currentUsers.deleteClient(client);
                logger.info(UNSUPPORTED_CLIENT);
                client.close();
                logger.info(CONNECTION_CLOSED);
                return;
            }

            handleService.execute(() -> {
                int requestId = currentRequests.getRequestId(client);
                UserContext context = currentUsers.getUserContext(client);
                logger.info(PROCESSING_REQUEST);
                String result = requestHandler.getHandleRequestResult(request, context);
                try {
                    responses.put(new Response(requestId, result));
                } catch (InterruptedException e) {
                    String message = String.format("Thread name: %s, message: %s", Thread.currentThread().getName(), BLOCK_QUEUE_PUT_ERROR);
                    logger.log(Level.SEVERE, message, e);
                }
            });
        } catch (IOException e) {
            logger.info(UNSUPPORTED_CLIENT);
            currentUsers.deleteClient(client);
            logger.info(DELETING_SUCCESSFUL);
            client.close();
            logger.info(CONNECTION_CLOSED);
            logger.log(Level.SEVERE, CHANNEL_FAILURE, e);
        }
    }

    @Override
    public void close() throws Exception {
        handleService.shutdown();
        try {
            serverSocketChannel.close();
            selector.close();
        } finally {
            if (serverSocketChannel.isOpen() || selector.isOpen()) {
                throw new ServerCloseException(SERVER_CLOSE_ERROR);
            }
        }
    }
}
