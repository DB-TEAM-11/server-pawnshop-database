package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class PlayerKeyByToken {
    private static final String QUERY = "SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = ?";

    public static int getPlayerKey(Connection connection, String sessionToken) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setString(1, sessionToken);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                return queryResult.getInt(1);
            }
        }
    }
}
