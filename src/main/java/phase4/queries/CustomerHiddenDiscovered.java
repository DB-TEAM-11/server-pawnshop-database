package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class CustomerHiddenDiscovered {
    private static final String QUERY = "SELECT HINT_REVEALED_FLAG FROM CUSTOMER_HIDDEN_DISCOVERED_IN_GAME_SESSION WHERE GAME_SESSION_KEY = ? AND CUSTOMER_KEY = ?";
    
    // MERGE query for insert or update
    private static final String MERGE_QUERY = "MERGE INTO CUSTOMER_HIDDEN_DISCOVERED_IN_GAME_SESSION CH USING (SELECT ? AS GAME_SESSION_KEY, ? AS CUSTOMER_KEY, ? AS HINT_REVEALED_FLAG FROM DUAL) SOURCE ON (CH.GAME_SESSION_KEY = SOURCE.GAME_SESSION_KEY AND CH.CUSTOMER_KEY = SOURCE.CUSTOMER_KEY) WHEN MATCHED THEN UPDATE SET CH.HINT_REVEALED_FLAG = SOURCE.HINT_REVEALED_FLAG WHEN NOT MATCHED THEN INSERT (GAME_SESSION_KEY, CUSTOMER_KEY, HINT_REVEALED_FLAG) VALUES (SOURCE.GAME_SESSION_KEY, SOURCE.CUSTOMER_KEY, SOURCE.HINT_REVEALED_FLAG)";

    public static int getHintRevealedFlag(Connection connection, int gameSessionKey, int customerKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, gameSessionKey);
            statement.setInt(2, customerKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                return queryResult.getInt(1);
            }
        }
    }

    public static void upsertHintRevealedFlag(Connection connection, int gameSessionKey, int customerKey, int hintRevealedFlag) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(MERGE_QUERY)) {
            statement.setInt(1, gameSessionKey);
            statement.setInt(2, customerKey);
            statement.setInt(3, hintRevealedFlag);
            statement.executeUpdate();
        }
    }
}
