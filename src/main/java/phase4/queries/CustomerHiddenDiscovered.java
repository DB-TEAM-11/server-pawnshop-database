package phase4.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase4.exceptions.NotASuchRowException;

public class CustomerHiddenDiscovered {
    private static final String QUERY = "SELECT HINT_REVEALED_FLAG FROM CUSTOMER_HIDDEN_DISCOVERED_IN_GAME_SESSION WHERE GAME_SESSION_KEY = %d AND CUSTOMER_KEY = %d";
    
    // MERGE query for insert or update
    private static final String MERGE_QUERY = "MERGE INTO CUSTOMER_HIDDEN_DISCOVERED_IN_GAME_SESSION CH USING (SELECT %d AS GAME_SESSION_KEY, %d AS CUSTOMER_KEY, %d AS HINT_REVEALED_FLAG FROM DUAL) SOURCE ON (CH.GAME_SESSION_KEY = SOURCE.GAME_SESSION_KEY AND CH.CUSTOMER_KEY = SOURCE.CUSTOMER_KEY) WHEN MATCHED THEN UPDATE SET CH.HINT_REVEALED_FLAG = SOURCE.HINT_REVEALED_FLAG WHEN NOT MATCHED THEN INSERT (GAME_SESSION_KEY, CUSTOMER_KEY, HINT_REVEALED_FLAG) VALUES (SOURCE.GAME_SESSION_KEY, SOURCE.CUSTOMER_KEY, SOURCE.HINT_REVEALED_FLAG)";

    public static int getHintRevealedFlag(Connection connection, int gameSessionKey, int customerKey) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, gameSessionKey, customerKey));
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }

        int hintRevealedFlag = queryResult.getInt(1);;

        statement.close();
        queryResult.close();
        return hintRevealedFlag;
    }

    public static void upsertHintRevealedFlag(Connection connection, int gameSessionKey, int customerKey, int hintRevealedFlag) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(MERGE_QUERY, gameSessionKey, customerKey, hintRevealedFlag));
        statement.close();
    }
}
