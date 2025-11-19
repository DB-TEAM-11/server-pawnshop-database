package queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import exceptions.NotASuchRowException;

public class PlayerKeyByToken {
    private static final String QUERY = "SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'";

    public static int getPlayerKey(Connection connection, String sessionToken) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, sessionToken));
        
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }
        
        int playerKey = queryResult.getInt(1);

        statement.close();
        queryResult.close();

        return playerKey;
    }
}
