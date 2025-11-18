package phase3.queries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateExistingItem {
    private static final String UPDATE_FLAW_QUERY = "UPDATE EXISTING_ITEM SET FOUND_FLAW_EA = FOUND_FLAW_EA + %d WHERE ITEM_KEY = %d";
    
    private static final String UPDATE_AUTHENTICITY_QUERY = "UPDATE EXISTING_ITEM SET IS_AUTHENTICITY_FOUND = 'Y' WHERE ITEM_KEY = %d";
    
    private static final String UPDATE_GRADE_QUERY = "UPDATE EXISTING_ITEM SET FOUND_GRADE = %d WHERE ITEM_KEY = %d";
    
    private static final String UPDATE_ITEM_STATE_QUERY = "UPDATE EXISTING_ITEM SET ITEM_STATE = %d WHERE ITEM_KEY = %d";

    public static void updateFoundFlaw(Connection connection, int itemKey, int additionalFlaws) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_FLAW_QUERY, additionalFlaws, itemKey));
        statement.close();
    }

    public static void updateAuthenticityFound(Connection connection, int itemKey) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_AUTHENTICITY_QUERY, itemKey));
        statement.close();
    }

    public static void updateFoundGrade(Connection connection, int itemKey, int grade) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_GRADE_QUERY, grade, itemKey));
        statement.close();
    }

    public static void updateItemState(Connection connection, int itemKey, int itemState) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_ITEM_STATE_QUERY, itemState, itemKey));
        statement.close();
    }
}
