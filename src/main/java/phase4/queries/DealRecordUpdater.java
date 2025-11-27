package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DealRecordUpdater {
    private static final String UPDATE_PRICES_QUERY = "UPDATE DEAL_RECORD SET PURCHASE_PRICE = ?, APPRAISED_PRICE = ? WHERE ITEM_KEY = ?";
    private static final String UPDATE_BOUGHT_DATE_QUERY = "UPDATE DEAL_RECORD SET BOUGHT_DATE = (SELECT G.DAY_COUNT FROM DEAL_RECORD D JOIN GAME_SESSION G ON D.GAME_SESSION_KEY = G.GAME_SESSION_KEY WHERE D.ITEM_KEY = ?) WHERE ITEM_KEY = ?";
    private static final String UPDATE_LAST_ACTION_DATE_QUERY = "UPDATE DEAL_RECORD SET LAST_ACTION_DATE = (SELECT G.DAY_COUNT FROM DEAL_RECORD D JOIN GAME_SESSION G ON D.GAME_SESSION_KEY = G.GAME_SESSION_KEY WHERE D.ITEM_KEY = ?) WHERE ITEM_KEY = ?";
    private static final String UPDATE_SOLD_QUERY = "UPDATE DEAL_RECORD SET ITEM_STATE = 4, SOLD_DATE = (SELECT G.DAY_COUNT FROM DEAL_RECORD D JOIN GAME_SESSION G ON D.GAME_SESSION_KEY = G.GAME_SESSION_KEY WHERE D.ITEM_KEY = ?), SELLING_PRICE = ?, BUYER_KEY = ? WHERE ITEM_KEY = ?";
    private static final String UPDATE_SOLD_QUERY_WITHOUT_BUYER = "UPDATE DEAL_RECORD SET ITEM_STATE = 4, SOLD_DATE = (SELECT G.DAY_COUNT FROM DEAL_RECORD D JOIN GAME_SESSION G ON D.GAME_SESSION_KEY = G.GAME_SESSION_KEY WHERE D.ITEM_KEY = ?), SELLING_PRICE = ? WHERE ITEM_KEY = ?";
    private static final String UPDATE_APPRAISED_PRICE = "UPDATE DEAL_RECORD SET APPRAISED_PRICE = ? WHERE ITEM_KEY = ?";
    
    public static void updatePrices(Connection connection, int itemKey, int purchasePrice, int appraisedPrice) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_PRICES_QUERY)) {
            statement.setInt(1, purchasePrice);
            statement.setInt(2, appraisedPrice);
            statement.setInt(3, itemKey);
            statement.executeUpdate();
        }
    }
    
    public static void updateBoughtDate(Connection connection, int itemKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_BOUGHT_DATE_QUERY)) {
            statement.setInt(1, itemKey);
            statement.setInt(2, itemKey);
            statement.executeUpdate();
        }
    }
    
    public static void updateLastActionDate(Connection connection, int itemKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_LAST_ACTION_DATE_QUERY)) {
            statement.setInt(1, itemKey);
            statement.setInt(2, itemKey);
            statement.executeUpdate();
        }
    }
    
    public static void updateAsSold(Connection connection, int itemKey, int sellingPrice, int buyerKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SOLD_QUERY)) {
            statement.setInt(1, itemKey);
            statement.setInt(2, sellingPrice);
            statement.setInt(3, buyerKey);
            statement.setInt(4, itemKey);
            statement.executeUpdate();
        }
    }
    
    public static void updateAsSold(Connection connection, int itemKey, int sellingPrice) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SOLD_QUERY_WITHOUT_BUYER)) {
            statement.setInt(1, itemKey);
            statement.setInt(2, sellingPrice);
            statement.setInt(3, itemKey);
            statement.executeUpdate();
        }
    }
    
    public static void updateAppraisedPrice(Connection connection, int itemKey, int appraisedPrice) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_APPRAISED_PRICE)) {
            statement.setInt(1, appraisedPrice);
            statement.setInt(2, itemKey);
            statement.executeUpdate();
        }
    }
}
