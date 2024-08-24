package server;

import exceptions.ClientDisconnectedException;
import exceptions.NotFullMessageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class MessageReader {
    private final HashMap<SocketChannel, ClientServiceInfo> clientInfoMap = new HashMap<>();

    public String getFullMessage(SocketChannel client) {
        try {
            int read;
            if (!clientInfoMap.containsKey(client)) {
                clientInfoMap.put(client, new ClientServiceInfo());
                ByteBuffer buffer = clientInfoMap.get(client).getBuffer();
                read = client.read(buffer);
                checkClientConnection(client, read);
                if (buffer.remaining() > 0) {
                    throw new NotFullMessageException();
                } else {
                    int messageLength = ByteBuffer.wrap(buffer.array()).getInt();
                    ClientServiceInfo clientServiceInfo = clientInfoMap.get(client);
                    clientServiceInfo.setNewBufferLength(messageLength);
                    clientServiceInfo.setReadyMessageBuffer(true);
                    buffer = clientServiceInfo.getBuffer();
                    read = client.read(buffer);
                    checkClientConnection(client, read);
                    if (buffer.remaining() == 0) {
                        return new String(buffer.array(), StandardCharsets.UTF_8);
                    } else {
                        throw new NotFullMessageException();
                    }
                }
            } else {
                ClientServiceInfo clientServiceInfo = clientInfoMap.get(client);
                ByteBuffer buffer = clientServiceInfo.getBuffer();
                read = client.read(buffer);
                checkClientConnection(client, read);
                if (buffer.remaining() > 0) {
                    throw new NotFullMessageException();
                } else {
                    if (buffer.capacity() == 4 && !clientServiceInfo.isMessageBufferReady()) {
                        int messageLength = ByteBuffer.wrap(buffer.array()).getInt();
                        clientServiceInfo.setNewBufferLength(messageLength);
                        clientServiceInfo.setReadyMessageBuffer(true);
                        buffer = clientServiceInfo.getBuffer();
                        read = client.read(buffer);
                        checkClientConnection(client, read);
                    }
                    if (buffer.remaining() == 0) {
                        byte[] bytes = buffer.array();
                        buffer.get(bytes, 0, 0);
                        clientServiceInfo.setNewBufferLength(4);
                        clientServiceInfo.setReadyMessageBuffer(false);
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
            clientInfoMap.remove(client);
            throw new ClientDisconnectedException();
        }
    }
}
