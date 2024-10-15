package server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ServerTest {
    RequestHandler requestHandler = Mockito.mock(RequestHandler.class);
    Server server;

    Socket firstSocket = new Socket();
    InputStream firstInputStream;
    OutputStream firstOutputStream;

    Socket secondSocket = new Socket();
    InputStream secondInputStream;
    OutputStream secondOutputStream;


    @BeforeEach
    void start() {
        new Thread(() -> {
            try {
                server = new Server(requestHandler, "localhost", 8888);
                server.start();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }).start();
    }

    @Test
    void test() throws IOException {

        Mockito.when(requestHandler.readGreetMessage()).thenReturn("read environment");
        Mockito.when(requestHandler.getHandleRequestResult(Mockito.anyString(), Mockito.any(UserContext.class))).thenReturn("good");
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        connectFirstClient();
        String greet = readMessageForFirstClient();
        Assertions.assertEquals("read environment", greet);
        sendFirstHalfRequestForFirstClient("clear");

        connectSecondClient();
        greet = readMessageForSecondClient();
        Assertions.assertEquals("read environment", greet);
        sendFirstHalfRequestForSecondClient("info");

        sendSecondHalfRequestForFirstClient("clear");
        String messageFirst = readMessageForFirstClient();
        Assertions.assertEquals("good", messageFirst);
        Mockito.verify(requestHandler).getHandleRequestResult(captor.capture(),Mockito.any(UserContext.class));
        Assertions.assertEquals("clear", captor.getValue());

        sendSecondHalfRequestForSecondClientByOneByte("info");
        String messageSecond = readMessageForSecondClient();
        Assertions.assertEquals("good", messageSecond);
        Mockito.verify(requestHandler, Mockito.times(2)).getHandleRequestResult(captor.capture(), Mockito.any(UserContext.class));
        Assertions.assertEquals("info", captor.getValue());


    }

    private void connectFirstClient() throws IOException {
        firstSocket.connect(new InetSocketAddress(InetAddress.getByName("localhost"), 8888), 10000);
        firstInputStream = firstSocket.getInputStream();
        firstOutputStream = firstSocket.getOutputStream();
    }

    private String readMessageForFirstClient() throws IOException {
        byte[] bytes = firstInputStream.readNBytes(4);
        int messageLength = ByteBuffer.wrap(bytes).getInt();
        bytes = new byte[messageLength];
        firstInputStream.readNBytes(bytes, 0, bytes.length);
        return new String(bytes);
    }

    private byte[] getMessageForSend(String request) {
        byte[] intValueBox = ByteBuffer.allocate(4).putInt(request.length()).array();
        byte[] requestBytes = request.getBytes(StandardCharsets.UTF_8);

        byte[] message = new byte[intValueBox.length + requestBytes.length];
        for (int i = 0; i < message.length; i++) {
            if (i < 4) {
                message[i] = intValueBox[i];
            } else {
                message[i] = requestBytes[i - 4];
            }
        }
        return message;
    }

    private void sendFirstHalfRequestForFirstClient(String request) throws IOException {
        byte[] message = getMessageForSend(request);

        for (int i = 0; i < message.length / 2; i++) {
            firstOutputStream.write(message[i]);
        }
        firstOutputStream.flush();
    }

    private void sendSecondHalfRequestForFirstClient(String request) throws IOException {
        byte[] message = getMessageForSend(request);
        for (int i = message.length / 2; i < message.length; i++) {
            firstOutputStream.write(message[i]);
        }
        firstOutputStream.flush();
    }

    private void connectSecondClient() throws IOException {
        secondSocket.connect(new InetSocketAddress(InetAddress.getByName("localhost"), 8888), 10000);
        secondInputStream = secondSocket.getInputStream();
        secondOutputStream = secondSocket.getOutputStream();
    }

    private String readMessageForSecondClient() throws IOException {
        byte[] bytes = secondInputStream.readNBytes(4);
        int messageLength = ByteBuffer.wrap(bytes).getInt();
        bytes = new byte[messageLength];
        secondInputStream.readNBytes(bytes, 0, bytes.length);
        return new String(bytes);
    }

    private void sendFirstHalfRequestForSecondClient(String request) throws IOException {
        byte[] message = getMessageForSend(request);

        for (int i = 0; i < message.length / 2; i++) {
            secondOutputStream.write(message[i]);
        }
        secondOutputStream.flush();
    }

    private void sendSecondHalfRequestForSecondClientByOneByte(String request) throws IOException {
        byte[] message = getMessageForSend(request);

        for (int i = message.length / 2; i < message.length; i++) {
            secondOutputStream.write(message[i]);
        }
        secondOutputStream.flush();
    }
}
