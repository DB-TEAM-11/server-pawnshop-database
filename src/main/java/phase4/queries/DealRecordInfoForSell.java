package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class DealRecordInfoForSell {
    private static final String QUERY = "SELECT DR.APPRAISED_PRICE, I.IS_AUTHENTICITY_FOUND FROM DEAL_RECORD DR, EXISTING_ITEM I WHERE I.ITEM_KEY = ? AND DR.DRC_KEY = I.DRC_KEY";

    public int appraisedPrice;
    public boolean isAuthenticityFound;

    private DealRecordInfoForSell(int appraisedPrice, boolean isAuthenticityFound) {
        this.appraisedPrice = appraisedPrice;
        this.isAuthenticityFound = isAuthenticityFound;
    }

    public static DealRecordInfoForSell getDealRecordInfoForSell(Connection connection, int itemKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, itemKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
        
                return new DealRecordInfoForSell(
                    queryResult.getInt(1),
                    queryResult.getString(2).equals("Y")
                );
            }
        }
    }
}
