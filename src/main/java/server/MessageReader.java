package server;

import exceptions.ClientDisconnectedException;
import exceptions.NotFullMessageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class MessageReader {
    private final HashMap<SocketChannel, ByteBuffer> clientBufferMap = new HashMap<>();

    public String getFullMessage(SocketChannel client) {
        try {
            int read;
            if (!clientBufferMap.containsKey(client)) {
                clientBufferMap.put(client, ByteBuffer.allocate(4));
                read = client.read(clientBufferMap.get(client));
                checkClientConnection(client, read);
                ByteBuffer buffer = clientBufferMap.get(client);
                if (buffer.remaining() > 0) {
                    throw new NotFullMessageException();
                } else {
                    int messageLength = ByteBuffer.wrap(clientBufferMap.get(client).array()).getInt();
                    clientBufferMap.put(client, ByteBuffer.allocate(messageLength));
                    read = client.read(clientBufferMap.get(client));
                    checkClientConnection(client, read);
                    buffer = clientBufferMap.get(client);
                    if (buffer.remaining() == 0) {
                        return new String(clientBufferMap.get(client).array(), StandardCharsets.UTF_8);
                    } else {
                        throw new NotFullMessageException();
                    }
                }

            } else {
                read = client.read(clientBufferMap.get(client));
                checkClientConnection(client, read);
                ByteBuffer buffer = clientBufferMap.get(client);
                if (buffer.remaining() > 0) {
                    throw new NotFullMessageException();
                } else {
                    if (buffer.capacity() == 4) {
                        int messageLength = ByteBuffer.wrap(clientBufferMap.get(client).array()).getInt();
                        clientBufferMap.put(client, ByteBuffer.allocate(messageLength));
                        read = client.read(clientBufferMap.get(client));
                        checkClientConnection(client, read);
                        buffer = clientBufferMap.get(client);
                    }
                    if (buffer.remaining() == 0) {
                        byte[] bytes = buffer.array();
                        buffer.get(bytes, 0, 0);
                        clientBufferMap.put(client, ByteBuffer.allocate(4));
                        return new String(bytes, StandardCharsets.UTF_8);
                    } else {
                        throw new NotFullMessageException();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private void checkClientConnection(SocketChannel client, int read) {
        if (read == -1) {
            clientBufferMap.remove(client);
            throw new ClientDisconnectedException();
        }
    }
}
