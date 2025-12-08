package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IdIsExist {
    private static final String QUERY = "SELECT HASHED_PW FROM PLAYER WHERE PLAYER_ID = ?";

    public static boolean isIdExist(Connection connection, String userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setString(1, userId);
            try (ResultSet queryResult = statement.executeQuery()) {
                return queryResult.next();
            }
        }
    }
}
