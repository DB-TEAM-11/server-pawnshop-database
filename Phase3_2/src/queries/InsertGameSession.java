package queries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InsertGameSession {
    private static final String INSERT_QUERY = "INSERT INTO GAME_SESSION (PLAYER_KEY, NICKNAME, SHOP_NAME) VALUES ((SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s'), '%s', '%s')";

    public static void insertGameSession(Connection connection, String sessionToken, String nickname, String shopName) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(INSERT_QUERY, sessionToken, nickname, shopName));
        statement.close();
    }
}
