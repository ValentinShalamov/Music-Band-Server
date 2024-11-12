package manager;

import music.MusicBand;
import user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        }
        return musicBands.stream()
                .sorted(Comparator.comparingLong(MusicBand::getId))
                .map(MusicBand::toString)
                .collect(Collectors.joining());
    }

    public String showMine(User user) {
        int userId = user.id();

        String result = musicBands.stream()
                .filter((band) -> band.getUser().id() == userId)
                .sorted(Comparator.comparingLong(MusicBand::getId))
                .map(MusicBand::toString)
                .collect(Collectors.joining());

        if (result.isEmpty()) {
            return NO_HAVE_BANDS;
        }
        return result;
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
        Predicate<MusicBand> removeLowerPredicate = mb -> mb.getUser().id() == userId && mb.getBestAlbum().sales() < sales;

        String result = musicBands.stream()
                .filter(removeLowerPredicate)
                .map(mb -> String.format(MUSIC_BAND_HAS_BEEN_DELETED_SUCCESSFUL_ID + "%d \n", mb.getId()))
                .collect(Collectors.joining());

        if (result.isEmpty()) {
            return ALL_ELEMENT_BIGGER;
        }
        musicBands.removeIf(removeLowerPredicate);
        return result;
    }

    public String showMin() {
        if (musicBands.isEmpty()) {
            return COLLECTION_IS_EMPTY;
        }
        return Collections.min(musicBands).toString();
    }

    public String filter(long sales) {
        if (musicBands.isEmpty()) {
            return COLLECTION_IS_EMPTY;
        }

        String result = musicBands.stream()
                .filter(mb -> mb.getBestAlbum().sales() == sales)
                .map(MusicBand::toString)
                .collect(Collectors.joining());

        if (result.isEmpty()) {
            return NO_SUCH_ELEMENTS;
        }

        return result;
    }

    public String printAsc() {
        if (musicBands.isEmpty()) {
            return COLLECTION_IS_EMPTY;
        }

        return musicBands.stream()
                .sorted(MusicBand::compareTo)
                .map(mb -> String.format("owner = %s, album name = %s, sales = %d \n", mb.getUser().login(), mb.getBestAlbum().name(), mb.getBestAlbum().sales()))
                .collect(Collectors.joining());
    }

    public String printDesc() {
        if (musicBands.isEmpty()) {
            return COLLECTION_IS_EMPTY;
        }

        return musicBands.stream()
                .sorted((mb1, mb2) -> Long.compare(mb2.getBestAlbum().sales(), mb1.getBestAlbum().sales()))
                .map(mb -> String.format("owner = %s, album name = %s, sales = %d \n", mb.getUser().login(), mb.getBestAlbum().name(), mb.getBestAlbum().sales()))
                .collect(Collectors.joining());
    }

    public void initBands(Set<MusicBand> anotherBand) {
        musicBands = anotherBand;
    }
}
