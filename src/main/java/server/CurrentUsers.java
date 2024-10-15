package server;

import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class CurrentUsers {

    private final HashMap<SocketChannel, UserContext> clientUserMap = new HashMap<>();

    public UserContext getUserContext(SocketChannel client) {
        return clientUserMap.get(client);
    }

    public void addClient(SocketChannel client) {
        clientUserMap.putIfAbsent(client, new UserContext());
    }

    public void deleteClient(SocketChannel client) {
        clientUserMap.remove(client);
    }
}
