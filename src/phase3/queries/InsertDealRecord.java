package phase3.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertDealRecord {
    private static final String INSERT_QUERY = "INSERT INTO DEAL_RECORD (GAME_SESSION_KEY, SELLER_KEY, ITEM_KEY, ASKING_PRICE, PURCHASE_PRICE, APPRAISED_PRICE) VALUES (?, ?, ?, ?, ?, ?)";

    public static void insertDealRecord(Connection connection, int gameSessionKey, int sellerKey, int itemKey, int askingPrice, int purchasePrice, int appraisedPrice) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(INSERT_QUERY);
        pstmt.setInt(1, gameSessionKey);
        pstmt.setInt(2, sellerKey);
        pstmt.setInt(3, itemKey);
        pstmt.setInt(4, askingPrice);
        pstmt.setInt(5, purchasePrice);
        pstmt.setInt(6, appraisedPrice);
        pstmt.executeUpdate();
        pstmt.close();
    }
}
