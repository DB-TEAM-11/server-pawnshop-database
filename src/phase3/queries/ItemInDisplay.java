package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import phase3.exceptions.NotASuchRowException;

public class ItemInDisplay {
    private static final String QUERY = "SELECT D.DISPLAY_POS, I.*, IC.* FROM GAME_SESSION_ITEM_DISPLAY D, EXISTING_ITEM I, ITEM_CATALOG IC WHERE D.GAME_SESSION_KEY = ( SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY ) AND D.ITEM_KEY = I.ITEM_KEY AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY ORDER BY D.DISPLAY_POS";

    public int displayPos;
    public int itemKey;
    public int gameSessionKey;
    public int itemCatalogKey;
    public int grade;
    public int foundGrade;
    public int flawEa;
    public int foundFlawEa;
    public float suspiciousFlawAura;
    public boolean authenticity;
    public boolean isAuthenticityFound;
    public int itemState;
    public String itemCatalogName;
    public String imgId;
    public int categoryKey;
    public int basePrice;

    private ItemInDisplay(
        int displayPos,
        int itemKey,
        int gameSessionKey,
        int itemCatalogKey,
        int grade,
        int foundGrade,
        int flawEa,
        int foundFlawEa,
        float suspiciousFlawAura,
        boolean authenticity,
        boolean isAuthenticityFound,
        int itemState,
        String itemCatalogName,
        String imgId,
        int categoryKey,
        int basePrice
    ) {
        this.displayPos = displayPos;
        this.itemKey = itemKey;
        this.gameSessionKey = gameSessionKey;
        this.itemCatalogKey = itemCatalogKey;
        this.grade = grade;
        this.foundGrade = foundGrade;
        this.flawEa = flawEa;
        this.foundFlawEa = foundFlawEa;
        this.suspiciousFlawAura = suspiciousFlawAura;
        this.authenticity = authenticity;
        this.isAuthenticityFound = isAuthenticityFound;
        this.itemState = itemState;
        this.itemCatalogName = itemCatalogName;
        this.imgId = imgId;
        this.categoryKey = categoryKey;
        this.basePrice = basePrice;
    }

    public static ItemInDisplay[] getItemInDisplay(Connection connection, int playerId) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, playerId));
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }

        ArrayList<ItemInDisplay> itemInDisplay = new ArrayList<ItemInDisplay>();
        do {
            itemInDisplay.add(new ItemInDisplay(
                queryResult.getInt(1),
                queryResult.getInt(2),
                queryResult.getInt(3),
                queryResult.getInt(4),
                queryResult.getInt(5),
                queryResult.getInt(6),
                queryResult.getInt(7),
                queryResult.getInt(8),
                queryResult.getFloat(9),
                queryResult.getString(10) == "Y",
                queryResult.getString(11) == "Y",
                queryResult.getInt(12),
                queryResult.getString(14),
                queryResult.getString(15),
                queryResult.getInt(16),
                queryResult.getInt(17)
            ));
        } while (queryResult.next());

        statement.close();
        queryResult.close();

        return itemInDisplay.toArray(new ItemInDisplay[0]);
    }
}
