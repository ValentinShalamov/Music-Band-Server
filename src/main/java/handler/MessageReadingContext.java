package handler;

import java.nio.ByteBuffer;

public class MessageReadingContext {
    private ByteBuffer buffer;
    private boolean isMessageBufferReady;

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

}
