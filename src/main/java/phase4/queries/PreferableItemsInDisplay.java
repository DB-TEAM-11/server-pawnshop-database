package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import phase4.exceptions.NotASuchRowException;

public class PreferableItemsInDisplay {
    private static final String QUERY = "SELECT * FROM CUSTOMER_CATALOG C, GAME_SESSION_ITEM_DISPLAY D, EXISTING_ITEM I, ITEM_CATALOG IC WHERE C.CUSTOMER_KEY = ? AND D.GAME_SESSION_KEY = ( SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = ? ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY ) AND D.ITEM_KEY = I.ITEM_KEY AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY AND IC.CATEGORY_KEY = C.CATEGORY_KEY ORDER BY D.DISPLAY_POS";

    public int customerKey;
    public String customerName;
    public int categoryKey;
    public String imgId;
    public float fraud;
    public float wellCollect;
    public float clumsy;
    public int displayKey;
    public int gameSessionKey;
    public int itemKey;
    public int displayPos;
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
    public int basePrice;

    private PreferableItemsInDisplay(
        int customerKey,
        String customerName,
        int categoryKey,
        String imgId,
        float fraud,
        float wellCollect,
        float clumsy,
        int displayKey,
        int gameSessionKey,
        int itemKey,
        int displayPos,
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
        int basePrice
    ) {
        this.customerKey = customerKey;
        this.customerName = customerName;
        this.categoryKey = categoryKey;
        this.imgId = imgId;
        this.fraud = fraud;
        this.wellCollect = wellCollect;
        this.clumsy = clumsy;
        this.displayKey = displayKey;
        this.gameSessionKey = gameSessionKey;
        this.itemKey = itemKey;
        this.displayPos = displayPos;
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
        this.basePrice = basePrice;
    }

    public static PreferableItemsInDisplay[] getPreferableItemsInDisplay(Connection connection, int customerId, int playerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, customerId);
            statement.setInt(2, playerId);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                ArrayList<PreferableItemsInDisplay> preferableItemsInDisplay = new ArrayList<PreferableItemsInDisplay>();
                do {
                    preferableItemsInDisplay.add(new PreferableItemsInDisplay(
                        queryResult.getInt(1),
                        queryResult.getString(2),
                        queryResult.getInt(3),
                        queryResult.getString(4),
                        queryResult.getFloat(5),
                        queryResult.getFloat(6),
                        queryResult.getFloat(7),
                        queryResult.getInt(8),
                        queryResult.getInt(9),
                        queryResult.getInt(10),
                        queryResult.getInt(11),
                        queryResult.getInt(14),
                        queryResult.getInt(15),
                        queryResult.getInt(16),
                        queryResult.getInt(17),
                        queryResult.getInt(18),
                        queryResult.getFloat(19),
                        queryResult.getString(20).equals("Y"),
                        queryResult.getString(21).equals("Y"),
                        queryResult.getInt(22),
                        queryResult.getString(24),
                        queryResult.getInt(27)
                    ));
                } while (queryResult.next());
                return preferableItemsInDisplay.toArray(new PreferableItemsInDisplay[0]);
            }
        }
    }
}
