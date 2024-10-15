package dao;

import exceptions.DatabaseValidationException;
import exceptions.MusicBandExistsException;
import logger.LoggerConfigurator;
import music.BestAlbum;
import music.MusicBand;
import music.MusicGenre;
import user.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static messages.ExceptionsDAOMessages.AFTER_ROLLBACK;
import static messages.ExceptionsDAOMessages.BEFORE_ROLLBACK;

public class MusicBandDAO {
    private final DatabaseConnector connector;
    private final Connection connection;
    private static final Logger logger = LoggerConfigurator.createDefaultLogger(MusicBandDAO.class.getName());

    public MusicBandDAO(DatabaseConnector connector) {
        this.connector = connector;
        this.connection = connector.getConnection();
    }

    public MusicBand insertBandAndSelect(MusicBand musicBand, User user) throws SQLException, MusicBandExistsException {
        try {
            connection.setAutoCommit(false);
            if (isNameBusy(musicBand.getName())) {
                throw new MusicBandExistsException();
            }
            final String insertSql = """
                    INSERT INTO music_bands (name, genre, number_participants, creation_date,
                    establishment_date, best_album_name, best_album_sales, owner_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)""";

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                int argCount = 0;
                preparedStatement.setString(++argCount, musicBand.getName());
                preparedStatement.setString(++argCount, musicBand.getGenre().toString());
                preparedStatement.setInt(++argCount, musicBand.getNumberOfParticipants());
                preparedStatement.setString(++argCount, musicBand.getCreationDate().toString());
                preparedStatement.setDate(++argCount, Date.valueOf(musicBand.getEstablishmentDate()));
                preparedStatement.setString(++argCount, musicBand.getBestAlbum().name());
                preparedStatement.setLong(++argCount, musicBand.getBestAlbum().sales());
                preparedStatement.setInt(++argCount, user.id());

                preparedStatement.executeUpdate();
            }

            final String selectSql = "SELECT band_id FROM music_bands WHERE name = ?";
            try (PreparedStatement secondPreparedStatement = connection.prepareStatement(selectSql)) {
                int argCount = 0;
                secondPreparedStatement.setString(++argCount, musicBand.getName());

                ResultSet resultSet = secondPreparedStatement.executeQuery();
                resultSet.next();
                long id = resultSet.getLong("band_id");
                musicBand.setId(id);
            }
            connection.commit();
            musicBand.setUser(user);
            return musicBand;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, BEFORE_ROLLBACK, e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, AFTER_ROLLBACK, e.getMessage());
                throw ex;
            }
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private boolean isNameBusy(String name) throws SQLException, MusicBandExistsException {
        final String sql = "SELECT name FROM music_bands WHERE name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int argCount = 0;
            preparedStatement.setString(++argCount, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw e;
        }
    }

    public boolean updateById(MusicBand musicBand, long id, User user) throws SQLException {
        final String sql = """
                UPDATE music_bands
                SET name = ?, genre = ?, number_participants = ?,
                creation_date = ?, establishment_date = ?,
                best_album_name = ?, best_album_sales = ?
                WHERE band_id = ? AND owner_id = ?""";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int argCount = 0;
            preparedStatement.setString(++argCount, musicBand.getName());
            preparedStatement.setString(++argCount, musicBand.getGenre().toString());
            preparedStatement.setInt(++argCount, musicBand.getNumberOfParticipants());
            preparedStatement.setString(++argCount, musicBand.getCreationDate().toString());
            preparedStatement.setDate(++argCount, Date.valueOf(musicBand.getEstablishmentDate()));
            preparedStatement.setString(++argCount, musicBand.getBestAlbum().name());
            preparedStatement.setLong(++argCount, musicBand.getBestAlbum().sales());
            preparedStatement.setLong(++argCount, id);
            preparedStatement.setInt(++argCount, user.id());

            return preparedStatement.executeUpdate() != 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw e;
        }
    }

    public boolean removeById(long id, User user) throws SQLException {
        final String sql = "DELETE FROM music_bands WHERE band_id = ? AND owner_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int argCount = 0;
            preparedStatement.setLong(++argCount, id);
            preparedStatement.setInt(++argCount, user.id());

            return preparedStatement.executeUpdate() != 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw e;
        }
    }

    public boolean clear(User user) throws SQLException {
        final String sql = "DELETE FROM music_bands WHERE owner_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int argCount = 0;
            preparedStatement.setInt(++argCount, user.id());

            return preparedStatement.executeUpdate() != 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw e;
        }
    }

    public MusicBand insertIfSalesMin(MusicBand musicBand, User user) throws SQLException, MusicBandExistsException, DatabaseValidationException {
        final String sql = "SELECT * FROM music_bands WHERE best_album_sales < ? AND owner_id = ?";
        connection.setAutoCommit(false);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int argCount = 0;
            preparedStatement.setLong(++argCount, musicBand.getBestAlbum().sales());
            preparedStatement.setInt(++argCount, user.id());

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return insertBandAndSelect(musicBand, user);
            } else {
                throw new DatabaseValidationException();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void removeLower(long sales, User user) throws SQLException {
        final String sql = "DELETE FROM music_bands WHERE best_album_sales < ? AND owner_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int argCount = 0;
            preparedStatement.setLong(++argCount, sales);
            preparedStatement.setInt(++argCount, user.id());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw e;
        }
    }

    public Set<MusicBand> readMusicBands() throws SQLException {
        Set<MusicBand> musicBands = new HashSet<>();
        final String sql = "SELECT music_bands.*, login, pass FROM music_bands JOIN owners ON music_bands.owner_id = owners.owner_id";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int argCount = 0;
                long id = resultSet.getLong(++argCount);
                String name = resultSet.getString(++argCount);
                MusicGenre genre = MusicGenre.valueOf(resultSet.getString(++argCount));
                int numberParticipants = resultSet.getInt(++argCount);
                LocalDateTime creationDate = LocalDateTime.parse(resultSet.getString(++argCount));
                LocalDate establishmentDate = LocalDate.parse(resultSet.getDate(++argCount).toString());
                String bestAlbumName = resultSet.getString(++argCount);
                long bestAlbumSales = resultSet.getLong(++argCount);
                int ownerId = resultSet.getInt(++argCount);
                String login = resultSet.getString(++argCount);
                String pass = resultSet.getString(++argCount);

                MusicBand musicBand = new MusicBand(id, name, genre, numberParticipants,
                        creationDate, establishmentDate, new BestAlbum(bestAlbumName, bestAlbumSales),
                        new User(ownerId, login, pass));
                musicBands.add(musicBand);
            }
            return musicBands;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage());
            throw e;
        }
    }
}
