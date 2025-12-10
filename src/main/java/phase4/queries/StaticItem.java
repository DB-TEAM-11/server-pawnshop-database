package phase4.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import phase4.exceptions.NotASuchRowException;

public class StaticItem {
    private static final String QUERY = "SELECT IC.ITEM_CATALOG_KEY, IC.ITEM_CATALOG_NAME, IC.IMG_ID, ICAT.CATEGORY_NAME FROM ITEM_CATALOG IC JOIN ITEM_CATEGORY ICAT ON IC.CATEGORY_KEY = ICAT.CATEGORY_KEY";

    public static StaticItem[] itemCatalog;

    public int itemCatalogKey;
    public String itemCatalogName;
    public String imgId;
    public String categoryName;

    private StaticItem(int itemCatalogKey, String itemCatalogName, String imgId, String categoryName) {
        this.itemCatalogKey = itemCatalogKey;
        this.itemCatalogName = itemCatalogName;
        this.imgId = imgId;
        this.categoryName = categoryName;
    }

    public static StaticItem[] loadAllItems(Connection connection) throws SQLException {
        if (itemCatalog != null) {
            return itemCatalog;
        }

        try (
            Statement statement = connection.createStatement();
            ResultSet queryResult = statement.executeQuery(QUERY)
        ) {
            if (!queryResult.next()) {
                throw new NotASuchRowException();
            }
            ArrayList<StaticItem> catalog = new ArrayList<StaticItem>();
            do {
                catalog.add(new StaticItem(
                    queryResult.getInt(1),
                    queryResult.getString(2),
                    queryResult.getString(3),
                    queryResult.getString(4)
                ));
            } while (queryResult.next());
            return catalog.toArray(new StaticItem[0]);
        }
    }
}
