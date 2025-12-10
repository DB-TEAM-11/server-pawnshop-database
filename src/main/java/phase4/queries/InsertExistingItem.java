package phase4.queries;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import oracle.jdbc.OracleTypes;

public class InsertExistingItem {
    private static final String INSERT_QUERY = "{ CALL INSERT INTO EXISTING_ITEM (GAME_SESSION_KEY, ITEM_CATALOG_KEY, GRADE, FOUND_GRADE, FLAW_EA, FOUND_FLAW_EA, SUSPICIOUS_FLAW_AURA, AUTHENTICITY, IS_AUTHENTICITY_FOUND, ITEM_STATE) VALUES (?, ?, ?, 0, ?, 0, ?, ?, 'N', 0) RETURNING ITEM_KEY INTO ? }";

    public static int insertItem(Connection connection, int gameSessionKey, int itemCatalogKey, int grade, int flawEa, float suspiciousFlawAura, char authenticity) throws SQLException {
        try (CallableStatement statement = connection.prepareCall(INSERT_QUERY)) {
            statement.setInt(1, gameSessionKey);
            statement.setInt(2, itemCatalogKey);
            statement.setInt(3, grade);
            statement.setInt(4, flawEa);
            statement.setFloat(5, suspiciousFlawAura);
            statement.setString(6, String.valueOf(authenticity));
            statement.registerOutParameter(7, OracleTypes.NUMBER);
            statement.execute();
            return statement.getInt(7);
        }
    }
}
