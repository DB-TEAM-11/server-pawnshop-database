package phase3.queries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DealRecordUpdater {
    private static final String UPDATE_PRICES_QUERY = "UPDATE DEAL_RECORD SET PURCHASE_PRICE = %d, APPRAISED_PRICE = %d WHERE DRC_KEY = %d";
    private static final String UPDATE_BOUGHT_DATE_QUERY = "UPDATE DEAL_RECORD SET BOUGHT_DATE = (SELECT DAY_COUNT FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY) WHERE DRC_KEY = %d";
    private static final String UPDATE_LAST_ACTION_DATE_QUERY = "UPDATE DEAL_RECORD SET LAST_ACTION_DATE = (SELECT DAY_COUNT FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY) WHERE DRC_KEY = (SELECT DRC_KEY FROM DEAL_RECORD WHERE ITEM_KEY = %d)";
    private static final String UPDATE_SOLD_QUERY = "UPDATE DEAL_RECORD SET SOLD_DATE = (SELECT DAY_COUNT FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY), SELLING_PRICE = %d, BUYER_KEY = %d WHERE DRC_KEY = (SELECT DRC_KEY FROM DEAL_RECORD WHERE ITEM_KEY = %d)";
    private static final String UPDATE_SOLD_QUERY_WITHOUT_BUYER = "UPDATE DEAL_RECORD SET SOLD_DATE = (SELECT DAY_COUNT FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY), SELLING_PRICE = %d WHERE DRC_KEY = (SELECT DRC_KEY FROM DEAL_RECORD WHERE ITEM_KEY = %d)";
    private static final String UPDATE_APPRAISED_PRICE = "UPDATE DEAL_RECORD SET APPRAISED_PRICE = %d WHERE DRC_KEY = (SELECT DRC_KEY FROM EXISTING_ITEM WHERE ITEM_KEY = %d)";

    public static void updatePrices(Connection connection, int drcKey, int purchasePrice, int appraisedPrice) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_PRICES_QUERY, purchasePrice, appraisedPrice, drcKey));
        statement.close();
    }

    public static void updateAppraisedPrice(Connection connection, int itemKey, int appraisedPrice) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_APPRAISED_PRICE, appraisedPrice, itemKey));
        statement.close();
    }


    public static void updateBoughtDate(Connection connection, String sessionToken, int drcKey) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_BOUGHT_DATE_QUERY, sessionToken, drcKey));
        statement.close();
    }

    public static void updateLastActionDate(Connection connection, String sessionToken, int itemKey) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_LAST_ACTION_DATE_QUERY, sessionToken, itemKey));
        statement.close();
    }

    public static void updateSoldInfo(Connection connection, String sessionToken, int itemKey, int sellingPrice, int buyerKey) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_SOLD_QUERY, sessionToken, sellingPrice, buyerKey, itemKey));
        statement.close();
    }

    public static void updateSoldInfo(Connection connection, String sessionToken, int itemKey, int sellingPrice) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_SOLD_QUERY_WITHOUT_BUYER, sessionToken, sellingPrice, itemKey));
        statement.close();
    }
}
