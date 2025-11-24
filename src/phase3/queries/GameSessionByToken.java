package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase3.exceptions.NotASuchRowException;

public class GameSessionByToken {
    private static final String QUERY = "SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";

    public static int getGameSessionKey(Connection connection, String sessionToken) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, sessionToken));
        
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }
        
        int gameSessionKey = queryResult.getInt(1);

        statement.close();
        queryResult.close();

        return gameSessionKey;
    }
}
