package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameSessionUpdater {
    private static final String UPDATE_DAY_COUNT_QUERY = "UPDATE GAME_SESSION SET DAY_COUNT = DAY_COUNT + 1 WHERE GAME_SESSION_KEY = ?";
    private static final String GET_DAY_COUNT_QUERY = "SELECT DAY_COUNT FROM GAME_SESSION WHERE GAME_SESSION_KEY = ?";
    private static final String UPDATE_GAME_END_QUERY = "UPDATE GAME_SESSION SET GAME_END_DAY_COUNT = ?, GAME_END_DATE = SYSDATE WHERE GAME_SESSION_KEY = ?";

    public static void incrementDayCount(Connection connection, int gameSessionKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_DAY_COUNT_QUERY)) {
            statement.setInt(1, gameSessionKey);
            statement.executeUpdate();
        }
    }

    public static void setGameEnd(Connection connection, int gameSessionKey, boolean isWon) throws SQLException {
        int dayCount;
        try (PreparedStatement statement = connection.prepareStatement(GET_DAY_COUNT_QUERY)) {
            statement.setInt(1, gameSessionKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                dayCount = queryResult.getInt(1);
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_GAME_END_QUERY)) {
            statement.setInt(1, isWon ? dayCount : -dayCount);
            statement.setInt(2, gameSessionKey);
            statement.executeUpdate();
        }
    }
}
