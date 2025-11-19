package queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import exceptions.NotASuchRowException;

public class NotFoundItem {
    private static final String QUERY = "(SELECT IC.ITEM_CATALOG_NAME FROM ITEM_CATALOG IC) MINUS ( SELECT IC.ITEM_CATALOG_NAME FROM GAME_SESSION G, EXISTING_ITEM I, ITEM_CATALOG IC WHERE G.PLAYER_KEY = %d AND G.GAME_SESSION_KEY = I.GAME_SESSION_KEY AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY )";

    public static String[] getNotFoundItemName(Connection connection, int playerId) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, playerId));
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }

        ArrayList<String> notFoundItemName = new ArrayList<String>();
        do {
            notFoundItemName.add(queryResult.getString(1));
        } while (queryResult.next());

        statement.close();
        queryResult.close();

        return notFoundItemName.toArray(new String[0]);
    }
}
