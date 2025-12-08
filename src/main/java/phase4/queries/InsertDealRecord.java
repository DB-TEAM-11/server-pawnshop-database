package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertDealRecord {
    private static final String INSERT_QUERY = "INSERT INTO DEAL_RECORD (GAME_SESSION_KEY, SELLER_KEY, ITEM_KEY, ASKING_PRICE, PURCHASE_PRICE, APPRAISED_PRICE) VALUES (?, ?, ?, ?, ?, ?)";

    public static void insertDealRecord(Connection connection, int gameSessionKey, int sellerKey, int itemKey, int askingPrice, int purchasePrice, int appraisedPrice) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_QUERY)) {
            statement.setInt(1, gameSessionKey);
            statement.setInt(2, sellerKey);
            statement.setInt(3, itemKey);
            statement.setInt(4, askingPrice);
            statement.setInt(5, purchasePrice);
            statement.setInt(6, appraisedPrice);
            statement.executeUpdate();
        }
    }
}
