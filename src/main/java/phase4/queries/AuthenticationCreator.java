package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuthenticationCreator {
    private static final String QUERY = "INSERT INTO PLAYER P ( P.PLAYER_ID,  P.HASHED_PW,  P.SESSION_TOKEN,  P.LAST_ACTIVITY ) VALUES (?, ?, ?, ?)";
    
    public static void createAuthentication(Connection connection, String id, String hashedPwWithSalt) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setString(1, id);
            statement.setString(2, hashedPwWithSalt);
            statement.setString(3, null);
            java.util.Date now = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(now.getTime());
            statement.setDate(4, sqlDate);
            
            statement.executeUpdate();
        }
    }
}
