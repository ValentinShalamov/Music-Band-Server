
import file.FileManager;
import manager.CollectionManager;
import manager.Manager;
import music.MusicBand;
import server.Server;
import server.RequestHandler;

import java.io.IOException;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) throws IOException {
        HashSet<MusicBand> musicBands = new HashSet<>();
        CollectionManager collectionManager = new CollectionManager(musicBands);
        FileManager fileManager = new FileManager(collectionManager);
        Manager manager = new Manager(collectionManager, fileManager);
        RequestHandler requestHandler = new RequestHandler(manager);
        Server server = new Server(requestHandler, "localhost", 8888);
        server.start();
    }
}
