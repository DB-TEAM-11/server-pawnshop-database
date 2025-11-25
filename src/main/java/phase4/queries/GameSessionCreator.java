package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GameSessionCreator {
    private static final String INSERT_QUERY = "INSERT INTO GAME_SESSION (PLAYER_KEY, NICKNAME, SHOP_NAME, UNLOCKED_SHOWCASE_COUNT) VALUES (?, ?, ?, 8)";
    
    public static void createGameSession(Connection connection, int playerKey, String nickname, String shopName) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(INSERT_QUERY);
        statement.setInt(1, playerKey);
        statement.setString(2, nickname);
        statement.setString(3, shopName);
        statement.executeUpdate();
        statement.close();
    }
}
