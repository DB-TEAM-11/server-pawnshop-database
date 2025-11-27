package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import phase4.constants.ItemState;


public class ExistingItemUpdater {
    private static final String REMOVE_FLAW_QUERY = "UPDATE EXISTING_ITEM SET FLAW_EA = 0, FOUND_FLAW_EA = 0 WHERE ITEM_KEY = ?";
    private static final String UPDATE_FLAW_QUERY = "UPDATE EXISTING_ITEM SET FOUND_FLAW_EA = FOUND_FLAW_EA + ? WHERE ITEM_KEY = ?";
    private static final String UPDATE_AUTHENTICITY_QUERY = "UPDATE EXISTING_ITEM SET IS_AUTHENTICITY_FOUND = 'Y' WHERE ITEM_KEY = ?";
    private static final String UPDATE_FOUND_GRADE_QUERY = "UPDATE EXISTING_ITEM SET FOUND_GRADE = ? WHERE ITEM_KEY = ?";
    private static final String UPDATE_ITEM_STATE_QUERY = "UPDATE EXISTING_ITEM SET ITEM_STATE = ? WHERE ITEM_KEY = ?";

    public static void removeAllFlaws(Connection connection, int itemKey) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(REMOVE_FLAW_QUERY);
        statement.setInt(1, itemKey);
        statement.executeUpdate();
    }

    public static void addToFoundFlaw(Connection connection, int itemKey, int additionalFlaws) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(UPDATE_FLAW_QUERY);
        statement.setInt(1, additionalFlaws);
        statement.setInt(2, itemKey);
        statement.executeUpdate();
    }

    public static void updateAuthenticityFound(Connection connection, int itemKey) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(UPDATE_AUTHENTICITY_QUERY);
        statement.setInt(1, itemKey);
        statement.executeUpdate();
    }

    public static void updateFoundGrade(Connection connection, int itemKey, int grade) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(UPDATE_FOUND_GRADE_QUERY);
        statement.setInt(1, grade);
        statement.setInt(2, itemKey);
        statement.executeUpdate();
    }

    public static void updateItemState(Connection connection, int itemKey, ItemState itemState) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(UPDATE_ITEM_STATE_QUERY);
        statement.setInt(1, itemState.value());
        statement.setInt(2, itemKey);
        statement.executeUpdate();
    }
}
