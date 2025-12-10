package phase4.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class NewsCatalog {
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM NEWS_CATALOG";

    private int gameSessionKey;
    private int newsCatalogKey;
    private int amount;

    private NewsCatalog(int gameSessionKey, int newsCatalogKey, int amount) {
        this.gameSessionKey = gameSessionKey;
        this.newsCatalogKey = newsCatalogKey;
        this.amount = amount;
    }

    public static int getCount(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet queryResult = statement.executeQuery(COUNT_QUERY);
            return queryResult.getInt(1);
        }
    }
}
