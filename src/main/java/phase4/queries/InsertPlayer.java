package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertPlayer {
    private static final String INSERT_QUERY = "INSERT INTO PLAYER(PLAYER_ID, HASHED_PW, SESSION_TOKEN, LAST_ACTIVITY) VALUES (?, ?, null, TO_DATE(?, 'YYYY-MM-DD'))";

    public static void insertPlayer(Connection connection, String playerId, String hashedPassword, String date) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_QUERY)) { 
            statement.setString(1, playerId);
            statement.setString(2, hashedPassword);
            statement.setString(3, date);
            statement.executeUpdate();
        }
    }
}
