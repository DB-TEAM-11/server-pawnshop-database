package phase4.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase4.exceptions.NotASuchRowException;

public class DealRecordInfoForSell {
    private static final String QUERY = "SELECT DR.APPRAISED_PRICE, I.IS_AUTHENTICITY_FOUND FROM DEAL_RECORD DR, EXISTING_ITEM I WHERE I.ITEM_KEY = %d AND DR.DRC_KEY = I.DRC_KEY";

    public int appraisedPrice;
    public boolean isAuthenticityFound;

    private DealRecordInfoForSell(int appraisedPrice, boolean isAuthenticityFound) {
        this.appraisedPrice = appraisedPrice;
        this.isAuthenticityFound = isAuthenticityFound;
    }

    public static DealRecordInfoForSell getDealRecordInfoForSell(Connection connection, int itemKey) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, itemKey));
        
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }

        DealRecordInfoForSell dealRecordInfo = new DealRecordInfoForSell(
            queryResult.getInt(1),
            queryResult.getString(2).equals("Y")
        );

        statement.close();
        queryResult.close();

        return dealRecordInfo;
    }
}
