package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class ItemCatalog {
    private static final String QUERY = "SELECT * FROM ITEM_CATALOG WHERE CATEGORY_KEY = ? ORDER BY DBMS_RANDOM.VALUE FETCH FIRST ROW ONLY";
    private static final String QUERY_BY_KEY = "SELECT * FROM ITEM_CATALOG WHERE ITEM_CATALOG_KEY = ?";

    public int itemCatalogKey;
    public String itemCatalogName;
    public String imgId;
    public int categoryKey;
    public int basePrice;

    private ItemCatalog(
        int itemCatalogKey,
        String itemCatalogName,
        String imgId,
        int categoryKey,
        int basePrice
    ) {
        this.itemCatalogKey = itemCatalogKey;
        this.itemCatalogName = itemCatalogName;
        this.imgId = imgId;
        this.categoryKey = categoryKey;
        this.basePrice = basePrice;
    }

    public static ItemCatalog getRandomItemByCategory(Connection connection, int categoryKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, categoryKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                return new ItemCatalog(
                    queryResult.getInt(1),
                    queryResult.getString(2),
                    queryResult.getString(3),
                    queryResult.getInt(4),
                    queryResult.getInt(5)
                );
            }
        }
    }

    public static ItemCatalog getItemByKey(Connection connection, int itemCatalogKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY_BY_KEY)) {
            statement.setInt(1, itemCatalogKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                return new ItemCatalog(
                    queryResult.getInt(1),
                    queryResult.getString(2),
                    queryResult.getString(3),
                    queryResult.getInt(4),
                    queryResult.getInt(5)
                );
            }
        }
    }
}
