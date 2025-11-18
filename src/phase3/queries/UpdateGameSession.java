package phase3.queries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateGameSession {
    private static final String UPDATE_DAY_COUNT_QUERY = "UPDATE GAME_SESSION SET DAY_COUNT = DAY_COUNT + 1 WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";
    
    private static final String UPDATE_GAME_END_QUERY = "UPDATE GAME_SESSION SET GAME_END_DAY_COUNT = %d, GAME_END_DATE = SYSDATE WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";

    public static void incrementDayCount(Connection connection, String sessionToken) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_DAY_COUNT_QUERY, sessionToken));
        statement.close();
    }

    public static void setGameEnd(Connection connection, String sessionToken, int dayCount) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_GAME_END_QUERY, dayCount, sessionToken));
        statement.close();
    }
}
