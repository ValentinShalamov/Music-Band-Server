package file;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import exceptions.EmptyFileException;
import manager.CollectionManager;
import music.MusicBand;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

import static messages.ResultMessages.*;

public class FileManager {
    private final CollectionManager collectionManager;

    public FileManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    public String save(String path) throws IOException, SecurityException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        if (file.canWrite()) {
            JsonElement jsonElement = JsonParser.parseString(gson.toJson(collectionManager.getMusicBands()));
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file))) {
                gson.toJson(jsonElement, writer);
                return SAVING_SUCCESSFUL + file.getAbsolutePath();
            }
        } else {
            throw new SecurityException(NOT_RIGHT_ACCESS_ON_WRITE);
        }
    }

    public HashSet<MusicBand> read(String path) throws JsonSyntaxException, SecurityException, IOException, EmptyFileException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException(NO_SUCH_FILE);
        }
        if (file.canRead()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file))) {
                Type musicBandsList = new TypeToken<HashSet<MusicBand>>() {
                }.getType();
                HashSet<MusicBand> bands = gson.fromJson(reader, musicBandsList);
                if (bands != null) {
                    return bands;
                } else {
                    throw new EmptyFileException(FILE_IS_EMPTY);
                }
            }
        } else {
            throw new SecurityException(NOT_RIGHT_ACCESS_ON_READ);
        }
    }
}
