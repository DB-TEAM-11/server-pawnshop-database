package phase4.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase4.exceptions.NotASuchRowException;

public class GetLastInsertedItemKey {
    private static final String QUERY = "SELECT ITEM_KEY FROM EXISTING_ITEM WHERE GAME_SESSION_KEY = %d ORDER BY ITEM_KEY DESC FETCH FIRST ROW ONLY";

    public static int getLastInsertedItemKey(Connection connection, int gameSessionKey) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, gameSessionKey));
        
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }
        
        int itemKey = queryResult.getInt(1);

        statement.close();
        queryResult.close();

        return itemKey;
    }
}
