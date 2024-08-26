package server;

import exceptions.ClientDisconnectedException;
import exceptions.NotFullMessageException;
import logger.LoggerConfigurator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static messages.ErrorMessages.DESERIALIZATION_ERROR;
import static messages.ErrorMessages.UNEXPECTED_ERROR;
import static messages.ServerMessages.*;

public class Server {
    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final RequestHandler requestHandler;
    private final MessageReader messageReader;

    private static final Logger logger = LoggerConfigurator.createDefaultLogger(Server.class.getName());

    public Server(RequestHandler requestHandler, String address, int port) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(address, port));
        this.serverSocketChannel.configureBlocking(false);
        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.requestHandler = requestHandler;
        this.messageReader = new MessageReader();
    }

    public void start() {
        try {
            logger.setUseParentHandlers(true);
            logger.info(SERVER_RUNNING);
            logger.setUseParentHandlers(false);
            Set<SelectionKey> selectedKeys;
            Iterator<SelectionKey> iter;
            SelectionKey key;
            while (true) {
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
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, UNEXPECTED_ERROR, e);
        }
    }

    private ByteBuffer getMessageForSend(String resultMessage) {
        byte[] intValueBox = ByteBuffer.allocate(4).putInt(resultMessage.length()).array();
        byte[] requestBytes = resultMessage.getBytes(StandardCharsets.UTF_8);
        byte[] message = Arrays.copyOf(intValueBox, intValueBox.length + requestBytes.length);
        System.arraycopy(requestBytes, 0, message, 4, requestBytes.length);
        return ByteBuffer.wrap(message);
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        logger.info(NEW_CLIENT_CONNECTED + client.getRemoteAddress() + "\n");
        client.write(getMessageForSend(requestHandler.readEnvironment()));
        System.out.println(NEW_CLIENT_CONNECTED);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        String request;
        try {
            logger.info(GETTING_REQUEST_FROM_CLIENT);
            request = messageReader.getFullMessage(client);
        } catch (NotFullMessageException e) {
            return;
        } catch (ClientDisconnectedException ex) {
            requestHandler.save();
            client.close();
            logger.info(CLIENT_HAS_DISCONNECTED);
            return;
        }
        logger.info(PROCESSING_REQUEST);
        String result = requestHandler.getHandleRequestResult(request);
        logger.log(Level.WARNING, result);
        logger.info(PREPARING_TO_SEND);
        client.write(getMessageForSend(result));
        if (result.equals(DESERIALIZATION_ERROR)) {
            messageReader.deleteClient(client);
            client.close();
        }
        logger.info(SENDING_ANSWER_TO_CLIENT);
    }
}
