import dao.DatabaseConnector;
import dao.MusicBandDAO;
import dao.UserDAO;
import logger.LoggerConfigurator;
import manager.CashManager;
import manager.ManagerDAO;
import manager.Manager;
import music.MusicBand;
import server.RequestHandler;
import server.Server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/music_bands";
        String user = "mb_app";
        String pass = "musicq11";

        Logger logger = LoggerConfigurator.createDefaultLogger(Main.class.getName());

        try (DatabaseConnector connector = new DatabaseConnector(url, user, pass)) {
            Server server = getServer(connector);
            server.start();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static Server getServer(DatabaseConnector connector) throws IOException, SQLException {
        HashSet<MusicBand> musicBands = new HashSet<>();
        CashManager cashManager = new CashManager(musicBands);
        UserDAO userDAO = new UserDAO(connector);
        MusicBandDAO musicBandDAO = new MusicBandDAO(connector);
        ManagerDAO managerDAO = new ManagerDAO(musicBandDAO, userDAO);
        Manager manager = new Manager(cashManager, managerDAO);
        manager.readBands();
        RequestHandler requestHandler = new RequestHandler(manager);
        return new Server(requestHandler, "localhost", 8888);
    }
}
