package fr.gravendev.sanctions;

import fr.gravendev.database.AData;
import fr.gravendev.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SanctionsData extends AData<SanctionMember> {

    public SanctionsData(DatabaseManager databaseManager) {
        super(databaseManager);
    }

    @Override
    protected boolean save(SanctionMember sanction, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO infractions(`uuid`, `user`, `moderator`, `type`, `reason`, `start`, `end`, `finished`)" +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)" +
                        "ON DUPLICATE KEY UPDATE end = VALUES(end), finished = VALUES(finished)");

        statement.setString(1, sanction.getUUID().toString());
        statement.setString(2, sanction.getUser());
        statement.setString(3, sanction.getModerator());
        statement.setString(4, sanction.getType().name());
        statement.setString(5, sanction.getReason());
        statement.setTimestamp(6, new Timestamp(sanction.getStart().getTime()));
        statement.setTimestamp(7, sanction.getEnd() != null ? new Timestamp(sanction.getEnd().getTime()) : null);
        statement.setBoolean(8, sanction.isFinished());
        statement.executeUpdate();
        return true;
    }

    @Override
    protected SanctionMember get(String userId, Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM infractions WHERE user = ? AND (END < NOW() OR END IS NULL) ORDER BY start DESC");

        statement.setString(1, userId);

        ResultSet resultSet = statement.executeQuery();
        return resultSetToSanctions(resultSet);
    }

    public List<SanctionMember> getAll(String userId, SanctionType type) {
        List<SanctionMember> sanctions = new ArrayList<>();
        try (Connection connection = getConnection()) {

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM infractions WHERE user = ? AND type = ? ORDER BY start DESC");

            statement.setString(1, userId);
            statement.setString(2, type.name());

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                SanctionMember sanctionMember = resultSetToSanctions(resultSet);
                sanctions.add(sanctionMember);
            }

        } catch (Exception ex) {

            ex.printStackTrace();
        }
        return sanctions;
    }

    public List<SanctionMember> getAllFinished() {
        List<SanctionMember> sanctions = new ArrayList<>();
        try (Connection connection = getConnection()) {

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM infractions WHERE END < NOW() AND FINISHED = 0");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                SanctionMember sanctionMember = resultSetToSanctions(resultSet);
                sanctions.add(sanctionMember);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sanctions;
    }


    public SanctionMember getLast(String userId, SanctionType infractionType) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM infractions WHERE user = ? AND type = ? ORDER BY start DESC");

            statement.setString(1, userId);
            statement.setString(2, infractionType.name());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSetToSanctions(resultSet);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    @Override
    protected void delete(SanctionMember obj, Connection connection) {

    }


    private SanctionMember resultSetToSanctions(ResultSet resultSet) throws SQLException {
        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
        String user = resultSet.getString("user");
        String moderator = resultSet.getString("moderator");
        SanctionType type = SanctionType.valueOf(resultSet.getString("type"));
        String reason = resultSet.getString("reason");
        Date start = new Date(resultSet.getTimestamp("start").getTime());
        Date end = resultSet.getTimestamp("end") != null ?
                new Date(resultSet.getTimestamp("end").getTime()) : null;
        boolean finished = resultSet.getBoolean("finished");

        return new SanctionMember(uuid, user, moderator, reason, type, start, end, finished);
    }
}
