package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class GameSessionGetter {
    private static final String QUERY_GAME_SESSION_BY_SESSION_TOKEN = "SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = ?) ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";
    private static final String QUERY_GAME_SESSION_BY_PLAYER_KEY = "SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = ? ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";

    public static int getGameSessionBySessionToken(Connection connection, String sessionToken) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY_GAME_SESSION_BY_SESSION_TOKEN)) {
            statement.setString(1, sessionToken);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                return queryResult.getInt(1);
            }
        }
    }
    
    public static int getGameSessionByPlayerKey(Connection connection, int playerKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY_GAME_SESSION_BY_PLAYER_KEY)) {
            statement.setInt(1, playerKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                return queryResult.getInt(1);
            }
        }
    }
}
