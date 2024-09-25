package dao;

import command.CommandDeserializer;
import exceptions.DatabaseValidationException;
import exceptions.MusicBandExistsException;
import music.BestAlbum;
import music.MusicBand;
import music.MusicGenre;
import user.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class MusicBandDAO {
    public DatabaseConnector connector;
    public Connection connection;

    public MusicBandDAO(DatabaseConnector connector) {
        this.connector = connector;
        this.connection = connector.getConnection();
    }

    public MusicBand insertBandAndSelect(MusicBand musicBand, User user) throws SQLException, MusicBandExistsException {
        checkBandByName(musicBand.getName());
        try {
            connection.setAutoCommit(false);
            String sql = "INSERT INTO music_bands (name, genre, number_participants, creation_date," +
                    " establishment_date, best_album_name, best_album_sales, owner_id)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, musicBand.getName());
                preparedStatement.setString(2, musicBand.getGenre().toString());
                preparedStatement.setInt(3, musicBand.getNumberOfParticipants());
                preparedStatement.setString(4, musicBand.getCreationDate().toString());
                preparedStatement.setDate(5, Date.valueOf(musicBand.getEstablishmentDate()));
                preparedStatement.setString(6, musicBand.getBestAlbum().name());
                preparedStatement.setLong(7, musicBand.getBestAlbum().sales());
                preparedStatement.setInt(8, user.getId());

                preparedStatement.executeUpdate();
            }

            sql = "SELECT band_id FROM music_bands WHERE name = ?";
            try (PreparedStatement secondPreparedStatement = connection.prepareStatement(sql)) {
                secondPreparedStatement.setString(1, musicBand.getName());

                ResultSet resultSet = secondPreparedStatement.executeQuery();
                resultSet.next();
                long id = resultSet.getLong("band_id");
                musicBand.setId(id);
            }
            connection.commit();
            connection.setAutoCommit(true);
            musicBand.setUser(user);
            return musicBand;
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException(e);
        }
    }

    private void checkBandByName(String name) throws SQLException, MusicBandExistsException {
        String sql = "SELECT name FROM music_bands WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                throw new MusicBandExistsException();
            }
        }
    }

    public boolean updateById(MusicBand musicBand, long id, User user) throws SQLException {
        String sql = "UPDATE music_bands" +
                " SET name = ?, genre = ?, number_participants = ?, creation_date = ?," +
                " establishment_date = ?, best_album_name = ?, best_album_sales = ?" +
                " WHERE band_id = ? AND owner_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, musicBand.getName());
            preparedStatement.setString(2, musicBand.getGenre().toString());
            preparedStatement.setInt(3, musicBand.getNumberOfParticipants());
            preparedStatement.setString(4, musicBand.getCreationDate().toString());
            preparedStatement.setDate(5, Date.valueOf(musicBand.getEstablishmentDate()));
            preparedStatement.setString(6, musicBand.getBestAlbum().name());
            preparedStatement.setLong(7, musicBand.getBestAlbum().sales());
            preparedStatement.setLong(8, id);
            preparedStatement.setInt(9, user.getId());

            return preparedStatement.executeUpdate() != 0;
        }
    }

    public boolean removeById(long id, User user) throws SQLException {
        String sql = "DELETE FROM music_bands WHERE band_id = ? AND owner_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.setInt(2, user.getId());

            return preparedStatement.executeUpdate() != 0;
        }
    }

    public boolean clear(User user) throws SQLException {
        String sql = "DELETE FROM music_bands WHERE owner_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, user.getId());

            return preparedStatement.executeUpdate() != 0;
        }
    }

    public MusicBand insertIfSalesMin(MusicBand musicBand, User user) throws SQLException, MusicBandExistsException, DatabaseValidationException {
        checkBandByName(musicBand.getName());
        String sql = "SELECT * FROM music_bands WHERE best_album_sales < ? AND owner_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, musicBand.getBestAlbum().sales());
            preparedStatement.setInt(2, user.getId());

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return insertBandAndSelect(musicBand, user);
            } else {
                throw new DatabaseValidationException();
            }
        }
    }

    public void removeLower(long sales, User user) throws SQLException {
        String sql = "DELETE FROM music_bands WHERE best_album_sales < ? AND owner_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, sales);
            preparedStatement.setInt(2, user.getId());

            preparedStatement.executeUpdate();
        }
    }

    public Set<MusicBand> readMusicBands() throws SQLException {
        CommandDeserializer commandDeserializer = new CommandDeserializer();
        Set<MusicBand> musicBands = new HashSet<>();
        String sql = "SELECT music_bands.*, login, pass FROM music_bands JOIN owners ON music_bands.owner_id = owners.owner_id";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long id = resultSet.getLong(1);
                String name = resultSet.getString(2);
                MusicGenre genre = MusicGenre.valueOf(resultSet.getString(3));
                int numberParticipants = resultSet.getInt(4);
                LocalDateTime creationDate = commandDeserializer.readLocalDateTime(resultSet.getString(5));
                LocalDate establishmentDate = commandDeserializer.readLocalDate(resultSet.getDate(6).toString());
                String bestAlbumName = resultSet.getString(7);
                long bestAlbumSales = resultSet.getLong(8);
                int ownerId = resultSet.getInt(9);
                String login = resultSet.getString(10);

                MusicBand musicBand = new MusicBand(id, name, genre, numberParticipants,
                        creationDate, establishmentDate, new BestAlbum(bestAlbumName, bestAlbumSales),
                        new User(ownerId, login));
                musicBands.add(musicBand);
            }
            return musicBands;
        }
    }
}
