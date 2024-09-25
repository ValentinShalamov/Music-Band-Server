package server;

import user.User;

import java.nio.ByteBuffer;

public class MessageReadingContext {
    private ByteBuffer buffer;
    private boolean isMessageBufferReady;
    private User user;

    public MessageReadingContext() {
        this.buffer = ByteBuffer.allocate(4);
        this.isMessageBufferReady = false;
    }

    public boolean isMessageBufferReady() {
        return this.isMessageBufferReady;
    }

    public void setReadyMessageBuffer(boolean flag) {
        this.isMessageBufferReady = flag;
    }

    public void setNewBufferWithLength(int length) {
        this.buffer = ByteBuffer.allocate(length);
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
