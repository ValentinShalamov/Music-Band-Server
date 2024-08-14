package manager;

import music.MusicBand;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static messages.ResultMessages.*;

public class CollectionManager {
    private final Set<MusicBand> musicBands;
    private final LocalDateTime localDateTime;
    private final ArrayDeque<String> history;

    public CollectionManager(Set<MusicBand> musicBands) {
        this.musicBands = musicBands;
        this.localDateTime = LocalDateTime.now();
        this.history = new ArrayDeque<>();
    }

    private void addHistory(String commandName) {
        history.addFirst(commandName);
        if (history.size() > 12) {
            history.removeLast();
        }
    }

    public String help() {
        addHistory(" - help \n");
        return LIST_OF_AVAILABLE_COMMAND;
    }

    public String info() {
        addHistory(" - info \n");
        String typeCollection = String.format("Type of collection is %s \n", musicBands.getClass().getTypeName());
        String countElements = String.format("Count of elements is %d \n", musicBands.size());
        String dateInit = String.format("Time init is %s \n", localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return typeCollection + countElements + dateInit;
    }

    public String show() {
        addHistory(" - show \n");
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

    public String add(MusicBand musicBand) {
        addHistory(" - add \n");
        initGlobId(musicBands);
        musicBand.initId();
        if (musicBands.add(musicBand)) {
            return MUSIC_BAND_HAS_BEEN_ADDED_SUCCESSFUL;
        } else {
            return ADDITION_MISTAKE;
        }
    }

    public String updateById(MusicBand musicBand, long id) {
        addHistory(" - update <id> \n");
        Optional<MusicBand> oldMb = musicBands.stream()
                .filter(mb -> mb.getId() == id)
                .findAny();
        if (oldMb.isPresent()) {
            musicBand.setId(id);
            if (musicBands.add(musicBand)) {
                musicBands.remove(oldMb.get());
                return MUSIC_BAND_HAS_BEEN_UPDATED_SUCCESSFUL;
            } else {
                return UPDATED_MISTAKE;
            }
        }
        return NO_SUCH_ID;
    }

    public String removeById(long id) {
        addHistory(" - remove <id> \n");
        if (musicBands.removeIf((band) -> band.getId() == id)) {
            return MUSIC_BAND_HAS_BEEN_DELETED_SUCCESSFUL;
        } else {
            return DELETED_MISTAKE;
        }
    }

    public String clear() {
        addHistory(" - clear \n");
        musicBands.clear();
        return COLLECTION_HAS_BEEN_DELETED;
    }

    public String history() {
        if (history.isEmpty()) {
            return HISTORY_IS_EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        String firstCommand = history.getFirst();
        builder.append(firstCommand).delete(firstCommand.length() - 2, firstCommand.length()).append(" - last command \n");
        history.stream()
                .skip(1)
                .forEach(builder::append);
        return builder.toString();
    }

    public String addIfMin(MusicBand musicBand) {
        addHistory(" - add_if_min \n");
        if (musicBands.isEmpty()) {
            return EMPTY_COLLECTION_MISTAKE;
        }
        initGlobId(musicBands);
        musicBand.initId();
        if (musicBand.getBestAlbum().sales() < Collections.min(musicBands).getBestAlbum().sales()) {
            if (musicBands.add(musicBand)) {
                return MUSIC_BAND_HAS_BEEN_ADDED_SUCCESSFUL;
            } else {
                return ADDITION_MISTAKE;
            }
        } else {
            return SALES_BIGGER_THAN_MIN_ELEMENT;
        }
    }

    public String removeLower(long sales) {
        addHistory("- remove_lower \n");
        if (musicBands.isEmpty()) {
            return COLLECTION_IS_EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        musicBands.stream()
                .filter(mb -> mb.getBestAlbum().sales() < sales)
                .forEach(mb -> builder.append(MUSIC_BAND_HAS_BEEN_DELETED_SUCCESSFUL_ID).append(mb.getId()).append('\n'));
        musicBands.removeIf(mb -> mb.getBestAlbum().sales() < sales);
        if (builder.toString().isEmpty()) {
            return ALL_ELEMENT_BIGGER;
        }
        return builder.toString();
    }

    public String minByBestAlbum() {
        addHistory(" - min_by_best_album \n");
        if (musicBands.isEmpty()) {
            return COLLECTION_IS_EMPTY;
        }
        return Collections.min(musicBands).toString();
    }

    public String filterByBestAlbum(long sales) {
        addHistory("- filter_by_sales \n");
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
        addHistory(" - print_field_asc_best_album \n");
        if (musicBands.isEmpty()) {
            return COLLECTION_IS_EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        musicBands.stream()
                .sorted(MusicBand::compareTo)
                .forEach(mb -> builder.append("id = ").append(mb.getId()).append(", sales = ").append(mb.getBestAlbum().sales()).append('\n'));
        return builder.toString();
    }

    public String readMusicBand(Set<MusicBand> anotherBand) {
        musicBands.clear();
        musicBands.addAll(anotherBand);
        initGlobId(anotherBand);
        return COLLECTION_HAS_BEEN_READ;
    }

    private void initGlobId(Set<MusicBand> bands) {
        MusicBand.setGlobId(bands.stream()
                .map(MusicBand::getId)
                .max(Long::compare)
                .orElse(0L));
    }

    public Set<MusicBand> getMusicBands() {
        return Collections.unmodifiableSet(musicBands);
    }

}
