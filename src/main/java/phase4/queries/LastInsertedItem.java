package phase4.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class LastInsertedItem {
    private static final String QUERY = "SELECT ITEM_KEY FROM EXISTING_ITEM WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = ? ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY) ORDER BY ITEM_KEY DESC FETCH FIRST ROW ONLY";

    public static int getLastInsertedItemKey(Connection connection, int playerKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
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
