package server;

import exceptions.ClientDisconnectedException;
import exceptions.NotFullMessageException;
import exceptions.UnsupportedClientException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class MessageReader {
    private final HashMap<SocketChannel, MessageReadingContext> clientInfoMap = new HashMap<>();
    private final int LIMIT_MESSAGE_LENGTH = 1024;

    public String getFullMessage(SocketChannel client) {
        try {
            int read;
            if (!clientInfoMap.containsKey(client)) {
                clientInfoMap.put(client, new MessageReadingContext());
                ByteBuffer buffer = clientInfoMap.get(client).getBuffer();
                read = client.read(buffer);
                checkClientConnection(client, read);
                if (buffer.remaining() > 0) {
                    throw new NotFullMessageException();
                } else {
                    int messageLength = ByteBuffer.wrap(buffer.array()).getInt();
                    if (messageLength > LIMIT_MESSAGE_LENGTH) {
                        throw new IllegalArgumentException();
                    }
                    MessageReadingContext messageReadingContext = clientInfoMap.get(client);
                    messageReadingContext.setNewBufferWithLength(messageLength);
                    messageReadingContext.setReadyMessageBuffer(true);
                    buffer = messageReadingContext.getBuffer();
                    read = client.read(buffer);
                    checkClientConnection(client, read);
                    if (buffer.remaining() == 0) {
                        byte[] bytes = buffer.array();
                        buffer.get(bytes, 0, 0);
                        messageReadingContext.setNewBufferWithLength(4);
                        messageReadingContext.setReadyMessageBuffer(false);
                        return new String(buffer.array(), StandardCharsets.UTF_8);
                    } else {
                        throw new NotFullMessageException();
                    }
                }
            } else {
                MessageReadingContext messageReadingContext = clientInfoMap.get(client);
                ByteBuffer buffer = messageReadingContext.getBuffer();
                read = client.read(buffer);
                checkClientConnection(client, read);
                if (buffer.remaining() > 0) {
                    throw new NotFullMessageException();
                } else {
                    if (buffer.capacity() == 4 && !messageReadingContext.isMessageBufferReady()) {
                        int messageLength = ByteBuffer.wrap(buffer.array()).getInt();
                        if (messageLength > LIMIT_MESSAGE_LENGTH) {
                            throw new IllegalArgumentException();
                        }
                        messageReadingContext.setNewBufferWithLength(messageLength);
                        messageReadingContext.setReadyMessageBuffer(true);
                        buffer = messageReadingContext.getBuffer();
                        read = client.read(buffer);
                        checkClientConnection(client, read);
                    }
                    if (buffer.remaining() == 0) {
                        byte[] bytes = buffer.array();
                        buffer.get(bytes, 0, 0);
                        messageReadingContext.setNewBufferWithLength(4);
                        messageReadingContext.setReadyMessageBuffer(false);
                        return new String(bytes, StandardCharsets.UTF_8);
                    } else {
                        throw new NotFullMessageException();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            clientInfoMap.remove(client);
            throw new UnsupportedClientException();
        }
        return null;
    }

    public void deleteClient(SocketChannel client) {
        clientInfoMap.remove(client);
    }

    private void checkClientConnection(SocketChannel client, int read) {
        if (read == -1) {
            clientInfoMap.remove(client);
            throw new ClientDisconnectedException();
        }
    }
}
