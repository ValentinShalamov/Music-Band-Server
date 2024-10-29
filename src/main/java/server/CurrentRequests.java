package server;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CurrentRequests {
    private final Map<Integer, SocketChannel> map = new ConcurrentHashMap<>();
    private final AtomicInteger atomicCounter = new AtomicInteger();

    public int getRequestId(SocketChannel client) {
        int id = atomicCounter.incrementAndGet();
        map.put(id, client);
        return id;
    }

    public SocketChannel takeClient(int id) {
        return map.remove(id);
    }
}
