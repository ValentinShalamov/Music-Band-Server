import dao.DatabaseConnector;
import dao.MusicBandDAO;
import dao.UserDAO;
import handler.MessageReader;
import handler.RequestHandler;
import handler.UserContext;
import logger.LoggerConfigurator;
import manager.CacheManager;
import manager.LoginAndRegisterManager;
import manager.MusicBandManager;
import multithreading.MultithreadedRequestHandler;
import music.MusicBand;
import response.Response;
import server.Server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/music_bands";
        String user = "mb_app";
        String pass = "musicq11";

        Logger logger = LoggerConfigurator.createDefaultLogger(Main.class.getName());

        try (DatabaseConnector connector = new DatabaseConnector(url, user, pass);
             Server server = getServer(connector)) {
            server.start();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static Server getServer(DatabaseConnector connector) throws IOException, SQLException {
        RequestHandler requestHandler = getRequestHandler(connector);
        Map<SocketChannel, UserContext> currentUsers = new ConcurrentHashMap<>();
        MessageReader messageReader = new MessageReader();
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        BlockingQueue<Response> responses = new ArrayBlockingQueue<>(100);
        MultithreadedRequestHandler multithreadedRequestHandler = new MultithreadedRequestHandler(threadPool, responses, requestHandler);

        return new Server(multithreadedRequestHandler, responses, messageReader,
                currentUsers,"193.124.115.131", 8888);
    }

    private static RequestHandler getRequestHandler(DatabaseConnector connector) throws SQLException {
        Set<MusicBand> musicBands = ConcurrentHashMap.newKeySet();
        CacheManager cacheManager = new CacheManager(musicBands);
        UserDAO userDAO = new UserDAO(connector);
        MusicBandDAO musicBandDAO = new MusicBandDAO(connector);
        LoginAndRegisterManager loginAndRegisterManager = new LoginAndRegisterManager(userDAO);
        MusicBandManager musicBandManager = new MusicBandManager(cacheManager, musicBandDAO);
        return new RequestHandler(musicBandManager, loginAndRegisterManager);
    }

}
