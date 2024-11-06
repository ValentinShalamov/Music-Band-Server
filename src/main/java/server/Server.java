package server;

import exceptions.ClientDisconnectedException;
import exceptions.NotFullMessageException;
import exceptions.ServerCloseException;
import exceptions.UnsupportedClientException;
import handler.MessageReader;
import handler.UserContext;
import logger.LoggerConfigurator;
import multithreading.MultithreadedRequestHandler;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static messages.ErrorMessages.*;
import static messages.ServerMessages.*;
import static messages.UserMessages.GREET_MESSAGE;

public class Server implements AutoCloseable {
    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final MessageReader messageReader;
    private final CurrentRequests currentRequests;
    private final BlockingQueue<Response> responses;
    private final MultithreadedRequestHandler multithreadedRequestHandler;
    private final Map<SocketChannel, UserContext> currentUsers;

    private static final Logger logger = LoggerConfigurator.createDefaultLogger(Server.class.getName());

    public Server(MultithreadedRequestHandler multithreadedRequestHandler, BlockingQueue<Response> blockingQueue,
                  MessageReader messageReader, Map<SocketChannel, UserContext> currentUsers,
                  String address, int port) throws IOException {

        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(address, port));
        this.serverSocketChannel.configureBlocking(false);
        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.messageReader = messageReader;
        this.currentUsers = currentUsers;
        this.currentRequests = new CurrentRequests();
        this.responses = blockingQueue;

        this.multithreadedRequestHandler = multithreadedRequestHandler;
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
                        currentUsers.remove(client);
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
            currentUsers.putIfAbsent(client, new UserContext());
            logger.info(NEW_CLIENT_CONNECTED + client.getRemoteAddress() + "\n");
            int requestId = currentRequests.getRequestId(client);
            responses.put(new Response(requestId, GREET_MESSAGE));
        } catch (IOException e) {
            currentUsers.remove(client);
            client.close();
            logger.info(CONNECTION_CLOSED);
            logger.log(Level.SEVERE, CHANNEL_FAILURE, e);
        } catch (InterruptedException e) {
            currentUsers.remove(client);
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
                currentUsers.remove(client);
                client.close();
                logger.info(CONNECTION_CLOSED);
                logger.info(CLIENT_HAS_DISCONNECTED);
                return;
            } catch (UnsupportedClientException e) {
                currentUsers.remove(client);
                logger.info(UNSUPPORTED_CLIENT);
                client.close();
                logger.info(CONNECTION_CLOSED);
                return;
            }

            multithreadedRequestHandler.execute(currentRequests.getRequestId(client), request, currentUsers.get(client));

        } catch (IOException e) {
            logger.info(UNSUPPORTED_CLIENT);
            currentUsers.remove(client);
            logger.info(DELETING_SUCCESSFUL);
            client.close();
            logger.info(CONNECTION_CLOSED);
            logger.log(Level.SEVERE, CHANNEL_FAILURE, e);
        }
    }

    @Override
    public void close() throws Exception {
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
