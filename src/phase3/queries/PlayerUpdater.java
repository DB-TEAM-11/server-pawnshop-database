package phase3.queries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PlayerUpdater {
    private static final String UPDATE_SESSION_TOKEN_QUERY = "UPDATE PLAYER SET SESSION_TOKEN = '%s', LAST_ACTIVITY = TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS') WHERE SESSION_TOKEN = '%s'";
    private static final String LOGOUT_QUERY = "UPDATE PLAYER SET SESSION_TOKEN = NULL WHERE SESSION_TOKEN = '%s'";

    public static void updateSessionToken(Connection connection, String newSessionToken, String datetime, String oldSessionToken) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_SESSION_TOKEN_QUERY, newSessionToken, datetime, oldSessionToken));
        statement.close();
    }

    public static void logout(Connection connection, String sessionToken) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(LOGOUT_QUERY, sessionToken));
        statement.close();
    }
}
