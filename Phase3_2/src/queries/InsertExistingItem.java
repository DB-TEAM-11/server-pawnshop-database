package queries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InsertExistingItem {
    private static final String INSERT_QUERY = "INSERT INTO EXISTING_ITEM (GAME_SESSION_KEY, ITEM_CATALOG_KEY, GRADE, FLAW_EA, SUSPICIOUS_FLAW_AURA, AUTHENTICITY, ITEM_STATE) VALUES (%d, %d, %d, %d, %f, '%s', 0)";

    public static void insertItem(Connection connection, int gameSessionKey, int itemCatalogKey, int grade, int flawEa, float suspiciousFlawAura, char authenticity) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(INSERT_QUERY, gameSessionKey, itemCatalogKey, grade, flawEa, suspiciousFlawAura, authenticity));
        statement.close();
    }
}
