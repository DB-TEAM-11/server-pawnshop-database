package phase3.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertExistingItem {
    private static final String INSERT_QUERY = "INSERT INTO EXISTING_ITEM (GAME_SESSION_KEY, ITEM_CATALOG_KEY, GRADE, FOUND_GRADE, FLAW_EA, FOUND_FLAW_EA, SUSPICIOUS_FLAW_AURA, AUTHENTICITY, IS_AUTHENTICITY_FOUND, ITEM_STATE) VALUES (?, ?, ?, 0, ?, 0, ?, ?, 'N', 0)";

    public static void insertItem(Connection connection, int gameSessionKey, int itemCatalogKey, int grade, int flawEa, float suspiciousFlawAura, char authenticity) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(INSERT_QUERY);
        pstmt.setInt(1, gameSessionKey);
        pstmt.setInt(2, itemCatalogKey);
        pstmt.setInt(3, grade);
        pstmt.setInt(4, flawEa);
        pstmt.setFloat(5, suspiciousFlawAura);
        pstmt.setString(6, String.valueOf(authenticity));
        pstmt.executeUpdate();
        pstmt.close();
    }
}
