package server;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CurrentRequests {
    private final Map<Integer, SocketChannel> map = new HashMap<>();
    private final Random random = new Random();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public int getRequestId(SocketChannel client) {
        readWriteLock.writeLock().lock();
        int id;
        try {
            id = generateId();
            map.put(id, client);
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return id;
    }

    public SocketChannel takeClient(int id) {
        readWriteLock.writeLock().lock();
        SocketChannel client;
        try {
            client = map.get(id);
            map.remove(id);
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return client;
    }

    private int generateId() {
        int id = random.nextInt(10000);
        while (map.containsKey(id)) {
            id = random.nextInt(10000);
        }
        return id;
    }
}
