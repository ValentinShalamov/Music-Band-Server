package handler;

import exceptions.ClientDisconnectedException;
import exceptions.NotFullMessageException;
import exceptions.UnsupportedClientException;
import logger.LoggerConfigurator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageReader {
    private final Map<SocketChannel, MessageReadingContext> readingContextMap = new HashMap<>();
    private static final int LIMIT_MESSAGE_LENGTH = 1024;
    private static final int REQUIRED_BUFFER_CAPACITY = 4;
    private static final Logger logger = LoggerConfigurator.createDefaultLogger(MessageReader.class.getName());

    public String getFullMessage(SocketChannel client) {
        try {
            int read;
            if (!readingContextMap.containsKey(client)) {
                readingContextMap.put(client, new MessageReadingContext());
                ByteBuffer buffer = readingContextMap.get(client).getBuffer();
                read = client.read(buffer);
                checkClientConnection(client, read);
                if (buffer.remaining() > 0) {
                    throw new NotFullMessageException();
                } else {
                    int messageLength = ByteBuffer.wrap(buffer.array()).getInt();
                    if (messageLength > LIMIT_MESSAGE_LENGTH) {
                        throw new IllegalArgumentException();
                    }
                    MessageReadingContext messageReadingContext = readingContextMap.get(client);
                    messageReadingContext.setNewBufferWithLength(messageLength);
                    messageReadingContext.setReadyMessageBuffer(true);
                    buffer = messageReadingContext.getBuffer();
                    read = client.read(buffer);
                    checkClientConnection(client, read);
                    if (buffer.remaining() == 0) {
                        byte[] bytes = buffer.array();
                        buffer.get(bytes, 0, 0);
                        readingContextMap.remove(client);
                        return new String(bytes, StandardCharsets.UTF_8);
                    } else {
                        throw new NotFullMessageException();
                    }
                }
            } else {
                MessageReadingContext messageReadingContext = readingContextMap.get(client);
                ByteBuffer buffer = messageReadingContext.getBuffer();
                read = client.read(buffer);
                checkClientConnection(client, read);
                if (buffer.remaining() > 0) {
                    throw new NotFullMessageException();
                } else {
                    if (buffer.capacity() == REQUIRED_BUFFER_CAPACITY && !messageReadingContext.isMessageBufferReady()) {
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
                        readingContextMap.remove(client);
                        return new String(bytes, StandardCharsets.UTF_8);
                    } else {
                        throw new NotFullMessageException();
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
            readingContextMap.remove(client);
            throw new UnsupportedClientException();
        } catch (IllegalArgumentException e) {
            readingContextMap.remove(client);
            throw new UnsupportedClientException();
        }
    }

    private void checkClientConnection(SocketChannel client, int read) {
        if (read == -1) {
            readingContextMap.remove(client);
            throw new ClientDisconnectedException();
        }
    }
}
