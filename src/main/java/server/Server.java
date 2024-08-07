package server;

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

import static messages.ServerMessages.*;

public class Server {
    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
    private final byte[] bytes = new byte[buffer.capacity()];
    private final RequestHandler requestHandler;

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
            showMessage(SERVER_RUNNING);
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
            showMessage(e.getMessage());
            showMessage(UNEXPECTED_ERROR);
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        showMessage(NEW_CLIENT_CONNECTED + client.getRemoteAddress());
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
            showMessage(CLIENT_HAS_DISCONNECTED);
        } else {
            buffer.flip();
            buffer.get(bytes, 0, buffer.remaining());
            request = new String(bytes, 0, r);
            result = requestHandler.getHandlerResult(requestHandler.handleRequest(request));
            client.write(ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8)));
            buffer.clear();
        }
    }

    private void showMessage(String message) {
        System.out.println(message);
    }
}
