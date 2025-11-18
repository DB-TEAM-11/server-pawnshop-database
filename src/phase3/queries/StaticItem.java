package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class StaticItem {
    private static String QUERY = "SELECT HASHED_PW FROM PLAYER WHERE PLAYER_ID = '%d'";

    public static StaticItem[] itemCatalog;

    private int key;
    private String name;
    private int imageId;
    private int categoryName;

    private StaticItem(int key, String name, int imageId, int categoryName) {
        this.key = key;
        this.name = name;
        this.imageId = imageId;
        this.categoryName = categoryName;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public int getImageId() {
        return imageId;
    }

    public int getCategoryName() {
        return categoryName;
    }

    public static StaticItem[] loadAllItems(Connection connection) throws SQLException {
        if (itemCatalog != null) {
            return itemCatalog;
        }
        Statement statement = connection.createStatement();
        ResultSet staticItems = statement.executeQuery("SELECT * FROM (ITEM_CATALOG IC JOIN ITEM_CATEGORY CT ON IC.CATEGORY_KEY = CT.CATEGORY_KEY)");
        if (!staticItems.next()) {
            return null;
        }

        ArrayList<StaticItem> catalog = new ArrayList<StaticItem>();
        do {
            catalog.add(new StaticItem(
                staticItems.getInt(1),
                staticItems.getString(2),
                staticItems.getInt(3),
                staticItems.getInt(4)
            ));
        } while (staticItems.next());

        statement.close();
        staticItems.close();

        itemCatalog = catalog.toArray(new StaticItem[0]);
        return itemCatalog;
    }
}
