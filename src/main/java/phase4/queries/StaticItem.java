package phase4.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import phase4.exceptions.NotASuchRowException;

public class StaticItem {
    private static final String QUERY = "SELECT "
    		+ "IC.ITEM_CATALOG_KEY, "
    		+ "IC.ITEM_CATALOG_NAME, "
    		+ "IC.IMG_ID, "
    		+ "ICAT.CATEGORY_NAME "
    		+ "FROM ITEM_CATALOG IC "
    		+ "JOIN ITEM_CATEGORY ICAT "
    		+ "ON IC.CATEGORY_KEY = ICAT.CATEGORY_KEY";

    public static StaticItem[] itemCatalog;

    public int key;
    public String name;
    public int imageId;
    public String categoryName;

    private StaticItem(int key, String name, int imageId, String categoryName) {
        this.key = key;
        this.name = name;
        this.imageId = imageId;
        this.categoryName = categoryName;
    }

    public static StaticItem[] loadAllItems(Connection connection) throws SQLException {
        if (itemCatalog != null) {
            return itemCatalog;
        }
        Statement statement = connection.createStatement();
        ResultSet staticItems = statement.executeQuery(QUERY);
        if (!staticItems.next()) {
            throw new NotASuchRowException();
        }
        
        ArrayList<StaticItem> catalog = new ArrayList<StaticItem>();
        do {
            catalog.add(new StaticItem(
                staticItems.getInt(1),
                staticItems.getString(2),
                staticItems.getInt(3),
                staticItems.getString(4)
            ));
        } while (staticItems.next());

        statement.close();
        staticItems.close();

        itemCatalog = catalog.toArray(new StaticItem[0]);
        return itemCatalog;
    }
}
