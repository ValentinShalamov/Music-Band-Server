package music;

import user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class MusicBand implements Comparable<MusicBand> {
    private Long id;
    private final String name;
    private final MusicGenre genre;
    private final int numberOfParticipants;
    private final java.time.LocalDateTime creationDate;
    private final java.time.LocalDate establishmentDate;
    private final BestAlbum bestAlbum;
    private User user;

    public MusicBand(Long id, String name, MusicGenre genre, int numberOfParticipants, LocalDateTime creationDate,
                     LocalDate establishmentDate,  BestAlbum bestAlbum, User user) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.numberOfParticipants = numberOfParticipants;
        this.creationDate = creationDate;
        this.establishmentDate = establishmentDate;
        this.bestAlbum = bestAlbum;
        this.user = user;
    }

    @Override
    public int compareTo(MusicBand anotherBand) {
        return Long.compare(bestAlbum.sales(), anotherBand.bestAlbum.sales());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public int getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public LocalDate getEstablishmentDate() {
        return establishmentDate;
    }

    public MusicGenre getGenre() {
        return genre;
    }

    public BestAlbum getBestAlbum() {
        return bestAlbum;
    }

    public User getUser() {
        return user;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return String.format("id = %d, name is %s, time creation is %s, " +
                "number of participants = %d, establishment date is %s, " +
                "genre is %s, the best album is %s, count sales of the best album = %d, " +
                        "user is %s \n",
                id, name, creationDate.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")), numberOfParticipants, establishmentDate, genre,
                bestAlbum.name(), bestAlbum.sales(), user.getLogin());
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

}
