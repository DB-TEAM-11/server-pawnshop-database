package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import phase4.exceptions.NotASuchRowException;

public class NotFoundItem {
    private static final String QUERY = "(SELECT IC.ITEM_CATALOG_NAME FROM ITEM_CATALOG IC) MINUS ( SELECT IC.ITEM_CATALOG_NAME FROM GAME_SESSION G, EXISTING_ITEM I, ITEM_CATALOG IC WHERE G.PLAYER_KEY = ? AND G.GAME_SESSION_KEY = I.GAME_SESSION_KEY AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY )";

    public static String[] getNotFoundItemName(Connection connection, int playerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, playerId);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                ArrayList<String> notFoundItemName = new ArrayList<String>();
                do {
                    notFoundItemName.add(queryResult.getString(1));
                } while (queryResult.next());
                return notFoundItemName.toArray(new String[0]);
            }
        }
    }
}
