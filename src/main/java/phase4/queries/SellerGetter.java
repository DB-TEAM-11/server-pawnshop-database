package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class SellerGetter {
    private static String QUERY = "SELECT DR.SELLER_KEY FROM DEAL_RECORD DR WHERE DR.ITEM_KEY = ?";

    public static int GetSellerKey(Connection connection, int itemKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, itemKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                return queryResult.getInt(1);
            }
        }
    }
}
