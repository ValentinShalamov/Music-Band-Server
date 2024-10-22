package server;

import handler.UserContext;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CurrentUsers {

    private final HashMap<SocketChannel, UserContext> clientUserMap = new HashMap<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public UserContext getUserContext(SocketChannel client) {
        readWriteLock.readLock().lock();
        try {
            return clientUserMap.get(client);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void addClient(SocketChannel client) {
        readWriteLock.writeLock().lock();
        try {
            clientUserMap.putIfAbsent(client, new UserContext());
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void deleteClient(SocketChannel client) {
        readWriteLock.writeLock().lock();
        try {
            clientUserMap.remove(client);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
