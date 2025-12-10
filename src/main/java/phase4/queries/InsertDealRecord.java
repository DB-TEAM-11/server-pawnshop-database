package phase4.queries;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import oracle.jdbc.OracleTypes;

public class InsertDealRecord {
    private static final String INSERT_QUERY = "{ CALL INSERT INTO DEAL_RECORD (GAME_SESSION_KEY, SELLER_KEY, ITEM_KEY, ASKING_PRICE, PURCHASE_PRICE, APPRAISED_PRICE) VALUES (?, ?, ?, ?, ?, ?) RETURNING DRC_KEY INTO ? }";

    public static int insertDealRecord(Connection connection, int gameSessionKey, int sellerKey, int itemKey, int askingPrice, int purchasePrice, int appraisedPrice) throws SQLException {
        try (CallableStatement statement = connection.prepareCall(INSERT_QUERY)) {
            statement.setInt(1, gameSessionKey);
            statement.setInt(2, sellerKey);
            statement.setInt(3, itemKey);
            statement.setInt(4, askingPrice);
            statement.setInt(5, purchasePrice);
            statement.setInt(6, appraisedPrice);
            statement.registerOutParameter(7, OracleTypes.NUMBER);
            statement.execute();
            return statement.getInt(7);
        }
    }
}
