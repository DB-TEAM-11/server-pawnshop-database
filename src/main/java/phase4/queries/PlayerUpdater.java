package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerUpdater {
    private static final String UPDATE_SESSION_TOKEN_QUERY = "UPDATE PLAYER SET SESSION_TOKEN = ?, LAST_ACTIVITY = TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS') WHERE SESSION_TOKEN = ?";
    private static final String LOGOUT_QUERY = "UPDATE PLAYER SET SESSION_TOKEN = NULL WHERE SESSION_TOKEN = ?";

    public static void updateSessionToken(Connection connection, String newSessionToken, String datetime, String oldSessionToken) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SESSION_TOKEN_QUERY)) { 
            statement.setString(1, newSessionToken);
            statement.setString(2, datetime);
            statement.setString(3, oldSessionToken);
            statement.executeUpdate();
        }
    }

    public static void logout(Connection connection, String sessionToken) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(LOGOUT_QUERY)) { 
            statement.setString(1, sessionToken);
            statement.executeUpdate();
        }
    }
}
