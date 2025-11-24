package phase4.queries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InsertPlayer {
    private static final String INSERT_QUERY = "INSERT INTO PLAYER(PLAYER_ID, HASHED_PW, SESSION_TOKEN, LAST_ACTIVITY) VALUES ('%s', '%s', null, TO_DATE('%s', 'YYYY-MM-DD'))";

    public static void insertPlayer(Connection connection, String playerId, String hashedPassword, String date) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(INSERT_QUERY, playerId, hashedPassword, date));
        statement.close();
    }
}
