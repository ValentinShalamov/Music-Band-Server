package manager;

import music.MusicBand;
import user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static messages.ResultMessages.*;

public class CacheManager {
    private Set<MusicBand> musicBands;
    private final LocalDateTime localDateTime;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public CacheManager(Set<MusicBand> musicBands) {
        this.musicBands = musicBands;
        this.localDateTime = LocalDateTime.now();
    }

    public String info() {
        readWriteLock.readLock().lock();
        try {
            String typeCollection = String.format("Type of collection is %s \n", musicBands.getClass().getTypeName());
            String countElements = String.format("Count of elements is %d \n", musicBands.size());
            String dateInit = String.format("Time init is %s \n", localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            return typeCollection + countElements + dateInit;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public String show() {
        readWriteLock.readLock().lock();
        try {
            if (musicBands.isEmpty()) {
                return COLLECTION_IS_EMPTY;
            } else {
                StringBuilder sb = new StringBuilder();
                musicBands.stream()
                        .sorted(Comparator.comparingLong(MusicBand::getId))
                        .forEach(sb::append);
                return sb.toString();
            }
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public String showMine(User user) {
        int userId = user.id();
        readWriteLock.readLock().lock();
        try {
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
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void add(MusicBand musicBand) {
        readWriteLock.writeLock().lock();
        try {
            musicBands.add(musicBand);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void updateById(MusicBand musicBand, long bandId) {
        removeById(bandId);
        musicBands.add(musicBand);
    }

    public void removeById(long bandId) {
        readWriteLock.writeLock().lock();
        try {
            musicBands.removeIf((band) -> band.getId() == bandId);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void clear(User user) {
        int userId = user.id();
        readWriteLock.writeLock().lock();
        try {
            musicBands.removeIf((band) -> band.getUser().id() == userId);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public String removeLower(long sales, User user) {
        int userId = user.id();
        StringBuilder builder;
        readWriteLock.readLock().lock();
        try {
            if (musicBands.stream()
                    .noneMatch(mb -> mb.getUser().id() == userId)) {
                return NO_HAVE_BANDS;
            }
            builder = new StringBuilder();
            musicBands.stream()
                    .filter(mb -> mb.getUser().id() == userId && mb.getBestAlbum().sales() < sales)
                    .forEach(mb -> builder.append(MUSIC_BAND_HAS_BEEN_DELETED_SUCCESSFUL_ID).append(mb.getId()).append('\n'));
        } finally {
            readWriteLock.readLock().unlock();
        }

        readWriteLock.writeLock().lock();
        try {
            musicBands.removeIf(mb -> mb.getUser().id() == userId && mb.getBestAlbum().sales() < sales);
        } finally {
            readWriteLock.writeLock().unlock();
        }
        if (builder.toString().isEmpty()) {
            return ALL_ELEMENT_BIGGER;
        }
        return builder.toString();
    }

    public String minByBestAlbum() {
        readWriteLock.readLock().lock();
        try {
            if (musicBands.isEmpty()) {
                return COLLECTION_IS_EMPTY;
            }
            return Collections.min(musicBands).toString();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public String filterByBestAlbum(long sales) {
        readWriteLock.readLock().lock();
        try {
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
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public String printFieldAscBestAlbum() {
        readWriteLock.readLock().lock();
        try {
            if (musicBands.isEmpty()) {
                return COLLECTION_IS_EMPTY;
            }
            StringBuilder builder = new StringBuilder();
            musicBands.stream()
                    .sorted(MusicBand::compareTo)
                    .forEach(mb -> builder.append("owner = ").append(mb.getUser().login()).append(", sales = ").append(mb.getBestAlbum().sales()).append('\n'));
            return builder.toString();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public void initBands(Set<MusicBand> anotherBand) {
        musicBands = anotherBand;
    }
}
