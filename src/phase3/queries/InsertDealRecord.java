package phase3.queries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InsertDealRecord {
    private static final String INSERT_QUERY = "INSERT INTO DEAL_RECORD (GAME_SESSION_KEY, SELLER_KEY, ITEM_KEY, ASKING_PRICE, PURCHASE_PRICE, APPRAISED_PRICE) VALUES (%d, %d, %d, %d, %d, %d)";

    public static void insertDealRecord(Connection connection, int gameSessionKey, int sellerKey, int itemKey, int askingPrice, int purchasePrice, int appraisedPrice) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(INSERT_QUERY, gameSessionKey, sellerKey, itemKey, askingPrice, purchasePrice, appraisedPrice));
        statement.close();
    }
}
