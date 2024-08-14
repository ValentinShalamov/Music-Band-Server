package logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerConfigurator {
    private static String path = "logg.log";
    private static Handler fileHandler;

    private static void initFileHandler() {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            fileHandler = new FileHandler(path);
        } catch (IOException e) {
            path = "temp_log_" + Math.round(Math.random() * 100);
            initFileHandler();
        }
    }

    private static Handler getFileHandler() {
        if (fileHandler == null) {
            initFileHandler();
            fileHandler.setFormatter(new SimpleFormatter());
        }
        return fileHandler;
    }

    public static Logger createDefaultLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.addHandler(getFileHandler());
        logger.setUseParentHandlers(false);
        return logger;
    }
}
