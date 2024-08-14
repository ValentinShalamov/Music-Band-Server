package server;

import logger.LoggerConfigurator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static messages.ErrorMessages.*;
import static messages.ServerMessages.*;

public class Server {
    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
    private final byte[] bytes = new byte[buffer.capacity()];
    private final RequestHandler requestHandler;

    private static final Logger logger = LoggerConfigurator.createDefaultLogger(Server.class.getName());

    public Server(RequestHandler requestHandler, String address, int port) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(address, port));
        this.serverSocketChannel.configureBlocking(false);
        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.requestHandler = requestHandler;
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

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        logger.info(NEW_CLIENT_CONNECTED + client.getRemoteAddress() + "\n");
        buffer.flip();
        client.write(ByteBuffer.wrap(requestHandler.readEnvironment().getBytes(StandardCharsets.UTF_8)));
        buffer.clear();
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        String request;
        String result;
        int r = client.read(buffer);
        if (r == -1) {
            requestHandler.save();
            client.close();
            logger.info(CLIENT_HAS_DISCONNECTED);
        } else {
            logger.info(GETTING_REQUEST_FROM_CLIENT);
            buffer.flip();
            buffer.get(bytes, 0, buffer.remaining());
            request = new String(bytes, 0, r);
            logger.info(PROCESSING_REQUEST);
            result = requestHandler.getHandleRequestResult(request);
            logger.log(Level.WARNING, result);
            logger.info(PREPARING_TO_SEND);
            client.write(ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8)));
            logger.info(SENDING_ANSWER_TO_CLIENT);
            buffer.clear();
        }
    }
}
