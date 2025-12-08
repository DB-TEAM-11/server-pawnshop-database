package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GameSessionUpdater {
    private static final String UPDATE_DAY_COUNT_QUERY = "UPDATE GAME_SESSION SET DAY_COUNT = DAY_COUNT + 1 WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = ?) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";
    private static final String UPDATE_GAME_END_QUERY = "UPDATE GAME_SESSION SET GAME_END_DAY_COUNT = ?, GAME_END_DATE = SYSDATE WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = ?) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";

    public static void incrementDayCount(Connection connection, String sessionToken) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_DAY_COUNT_QUERY)) {
            statement.setString(1, sessionToken);
            statement.executeUpdate();
        }
    }

    public static void setGameEnd(Connection connection, String sessionToken, int dayCount) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_GAME_END_QUERY)) {
            statement.setInt(1, dayCount);
            statement.setString(2, sessionToken);
            statement.executeUpdate();
        }
    }
}
