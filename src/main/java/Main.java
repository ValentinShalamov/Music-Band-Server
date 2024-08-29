import file.FileManager;
import logger.LoggerConfigurator;
import manager.CollectionManager;
import manager.Manager;
import music.MusicBand;
import server.RequestHandler;
import server.Server;

import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Logger logger = LoggerConfigurator.createDefaultLogger(Main.class.getName());
        try {
            logger = LoggerConfigurator.createDefaultLogger(Main.class.getName());
            HashSet<MusicBand> musicBands = new HashSet<>();
            CollectionManager collectionManager = new CollectionManager(musicBands);
            FileManager fileManager = new FileManager(collectionManager);
            Manager manager = new Manager(collectionManager, fileManager);
            RequestHandler requestHandler = new RequestHandler(manager);
            Server server = new Server(requestHandler, "193.124.115.131", 8888);
            server.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
