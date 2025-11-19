package queries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DeleteDeal {
    private static final String DELETE_ITEM_QUERY = "DELETE FROM EXISTING_ITEM WHERE ITEM_KEY = %d";
    private static final String DELETE_DEAL_QUERY = "DELETE FROM DEAL_RECORD WHERE DRC_KEY = %d";

    public static void deleteItem(Connection connection, int itemKey) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(DELETE_ITEM_QUERY, itemKey));
        statement.close();
    }

    public static void deleteDealRecord(Connection connection, int drcKey) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(DELETE_DEAL_QUERY, drcKey));
        statement.close();
    }
}
