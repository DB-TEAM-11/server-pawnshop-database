package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import phase3.exceptions.NotASuchRowException;

public class ItemCatalog {
    private static final String QUERY = "SELECT * FROM ITEM_CATALOG WHERE CATEGORY_KEY = %d ORDER BY DBMS_RANDOM.VALUE FETCH FIRST ROW ONLY";

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
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, categoryKey));
        
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }

        ItemCatalog itemCatalog = new ItemCatalog(
            queryResult.getInt(1),
            queryResult.getString(2),
            queryResult.getString(3),
            queryResult.getInt(4),
            queryResult.getInt(5)
        );

        statement.close();
        queryResult.close();

        return itemCatalog;
    }
}
