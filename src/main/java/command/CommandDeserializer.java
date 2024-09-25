package command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import adapters.LocalDateAdapter;
import adapters.LocalDateTimeAdapter;
import music.MusicBand;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CommandDeserializer {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    public Command deserialize(String serializeCommand) {
        return gson.fromJson(serializeCommand, Command.class);
    }

    public MusicBand readMusicBand(Command command) {
        return gson.fromJson(command.getFirstArg(), MusicBand.class);
    }

    public LocalDateTime readLocalDateTime(String string) {
        return gson.fromJson(gson.toJson(string), LocalDateTime.class);
    }

    public LocalDate readLocalDate(String string) {
        return gson.fromJson(gson.toJson(string), LocalDate.class);
    }

}
