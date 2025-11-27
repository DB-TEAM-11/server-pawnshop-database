package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteDeal {
    private static final String DELETE_ITEM_QUERY = "DELETE FROM EXISTING_ITEM WHERE ITEM_KEY = ?";
    private static final String DELETE_DEAL_QUERY = "DELETE FROM DEAL_RECORD WHERE ITEM_KEY = ?";

    public static void deleteItem(Connection connection, int itemKey) throws SQLException {
        deleteDealRecord(connection, itemKey);
        try (PreparedStatement statement = connection.prepareStatement(DELETE_ITEM_QUERY)) {
            statement.setInt(1, itemKey);
            statement.executeUpdate();
        }
    }

    public static void deleteDealRecord(Connection connection, int itemKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_DEAL_QUERY)) {
            statement.setInt(1, itemKey);
            statement.executeUpdate();
        }
    }
}
