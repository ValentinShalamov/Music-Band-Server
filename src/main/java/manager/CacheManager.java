package manager;

import music.MusicBand;
import user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import static messages.ResultMessages.*;

public class CacheManager {
    private Set<MusicBand> musicBands;
    private final LocalDateTime localDateTime;

    public CacheManager(Set<MusicBand> musicBands) {
        this.musicBands = musicBands;
        this.localDateTime = LocalDateTime.now();
    }

    public String info() {
        String typeCollection = String.format("Type of collection is %s \n", musicBands.getClass().getTypeName());
        String countElements = String.format("Count of elements is %d \n", musicBands.size());
        String dateInit = String.format("Time init is %s \n", localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return typeCollection + countElements + dateInit;
    }

    public String show() {
        if (musicBands.isEmpty()) {
            return COLLECTION_IS_EMPTY;
        } else {
            StringBuilder sb = new StringBuilder();
            musicBands.stream()
                    .sorted(Comparator.comparingLong(MusicBand::getId))
                    .forEach(sb::append);
            return sb.toString();
        }
    }

    public String showMine(User user) {
        int userId = user.id();
        if (musicBands.stream()
                .noneMatch(mb -> mb.getUser().id() == userId)) {
            return NO_HAVE_BANDS;
        } else {
            StringBuilder sb = new StringBuilder();
            musicBands.stream()
                    .filter((band) -> band.getUser().id() == userId)
                    .sorted(Comparator.comparingLong(MusicBand::getId))
                    .forEach(sb::append);
            return sb.toString();
        }
    }

    public void add(MusicBand musicBand) {
        musicBands.add(musicBand);
    }

    public void updateById(MusicBand musicBand, long bandId) {
        removeById(bandId);
        musicBands.add(musicBand);
    }

    public void removeById(long bandId) {
        musicBands.removeIf((band) -> band.getId() == bandId);
    }

    public void clear(User user) {
        musicBands.removeIf((band) -> band.getUser().id() == user.id());
    }

    public String removeLower(long sales, User user) {
        int userId = user.id();
        StringBuilder builder;

        if (musicBands.stream()
                .noneMatch(mb -> mb.getUser().id() == userId)) {
            return NO_HAVE_BANDS;
        }
        builder = new StringBuilder();
        musicBands.stream()
                .filter(mb -> mb.getUser().id() == userId && mb.getBestAlbum().sales() < sales)
                .forEach(mb -> builder.append(MUSIC_BAND_HAS_BEEN_DELETED_SUCCESSFUL_ID).append(mb.getId()).append('\n'));


        musicBands.removeIf(mb -> mb.getUser().id() == userId && mb.getBestAlbum().sales() < sales);

        if (builder.toString().isEmpty()) {
            return ALL_ELEMENT_BIGGER;
        }
        return builder.toString();
    }

    public String minByBestAlbum() {
        if (musicBands.isEmpty()) {
            return COLLECTION_IS_EMPTY;
        }
        return Collections.min(musicBands).toString();
    }

    public String filterByBestAlbum(long sales) {
        if (musicBands.isEmpty()) {
            return COLLECTION_IS_EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        musicBands.stream()
                .filter(mb -> mb.getBestAlbum().sales() == sales)
                .forEach(builder::append);
        if (builder.isEmpty()) {
            return NO_SUCH_ELEMENTS;
        }
        return builder.toString();
    }

    public String printFieldAscBestAlbum() {
        if (musicBands.isEmpty()) {
            return COLLECTION_IS_EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        musicBands.stream()
                .sorted(MusicBand::compareTo)
                .forEach(mb -> builder.append("owner = ").append(mb.getUser().login()).append(", sales = ").append(mb.getBestAlbum().sales()).append('\n'));
        return builder.toString();
    }

    public void initBands(Set<MusicBand> anotherBand) {
        musicBands = anotherBand;
    }
}
