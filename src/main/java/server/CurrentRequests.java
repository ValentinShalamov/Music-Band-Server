package server;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CurrentRequests {
    private final Map<Integer, SocketChannel> currentRequestsMap = new ConcurrentHashMap<>();
    private final AtomicInteger atomicCounter = new AtomicInteger();

    public int getRequestId(SocketChannel client) {
        int id = atomicCounter.incrementAndGet();
        currentRequestsMap.put(id, client);
        return id;
    }

    public SocketChannel takeClient(int id) {
        return currentRequestsMap.remove(id);
    }
}
