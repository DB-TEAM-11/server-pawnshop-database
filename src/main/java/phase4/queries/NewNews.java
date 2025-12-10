package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NewNews {
    private static final String QUERY = "INSERT INTO EXISTING_NEWS (GAME_SESSION_KEY, NCAT_KEY, AMOUNT) VALUES (?, ?, ?)";

    private int gameSessionKey;
    private int newsCatalogKey;
    private int amount;

    public NewNews(int gameSessionKey, int newsCatalogKey, int amount) {
        this.gameSessionKey = gameSessionKey;
        this.newsCatalogKey = newsCatalogKey;
        this.amount = amount;
    }

    public void insert(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, gameSessionKey);
            statement.setInt(2, newsCatalogKey);
            statement.setInt(3, amount);
            statement.executeUpdate();
        }
    }
}
