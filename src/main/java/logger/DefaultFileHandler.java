package logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class DefaultFileHandler {
    private static final Logger logger = Logger.getLogger(DefaultFileHandler.class.getName());
    private static final String path = "logg.log";
    private static Handler fileHandler;

    private DefaultFileHandler() {
    }

    private static void initHandler() {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            fileHandler = new FileHandler(path);
        } catch (IOException e) {
            logger.log(Level.WARNING, "The unexpected error ", new RuntimeException(e));
        }
    }

    public static Handler getFileHandler() {
        if (fileHandler == null) {
            initHandler();
            fileHandler.setFormatter(new SimpleFormatter());
        }
        return fileHandler;
    }
}
