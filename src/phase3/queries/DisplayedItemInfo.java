package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase3.exceptions.NotASuchRowException;

public class DisplayedItemInfo {
    private static final String QUERY = "SELECT D.DISPLAY_POS, I.*, IC.*, DR.PURCHASE_PRICE, DR.ASKING_PRICE, DR.APPRAISED_PRICE, DR.BOUGHT_DATE, CC.CUSTOMER_NAME, GS.MONEY FROM GAME_SESSION_ITEM_DISPLAY D, EXISTING_ITEM I, ITEM_CATALOG IC, DEAL_RECORD DR, CUSTOMER_CATALOG CC, GAME_SESSION GS WHERE D.ITEM_KEY = %d AND D.ITEM_KEY = I.ITEM_KEY AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY AND I.ITEM_KEY = DR.ITEM_KEY AND DR.SELLER_KEY = CC.CUSTOMER_KEY AND GS.GAME_SESSION_KEY = DR.GAME_SESSION_KEY";

    public int displayPos;
    public int itemKey;
    public int gameSessionKey;
    public int itemCatalogKey;
    public int grade;
    public int foundGrade;
    public int flawEa;
    public int foundFlawEa;
    public float suspiciousFlawAura;
    public char authenticity;
    public char isAuthenticityFound;
    public int itemState;
    public String itemCatalogName;
    public String imgId;
    public int categoryKey;
    public int basePrice;
    public int purchasePrice;
    public int askingPrice;
    public int appraisedPrice;
    public int boughtDate;
    public String customerName;
    public int money;

    private DisplayedItemInfo(
        int displayPos,
        int itemKey,
        int gameSessionKey,
        int itemCatalogKey,
        int grade,
        int foundGrade,
        int flawEa,
        int foundFlawEa,
        float suspiciousFlawAura,
        char authenticity,
        char isAuthenticityFound,
        int itemState,
        String itemCatalogName,
        String imgId,
        int categoryKey,
        int basePrice,
        int purchasePrice,
        int askingPrice,
        int appraisedPrice,
        int boughtDate,
        String customerName,
        int money
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
        this.purchasePrice = purchasePrice;
        this.askingPrice = askingPrice;
        this.appraisedPrice = appraisedPrice;
        this.boughtDate = boughtDate;
        this.customerName = customerName;
        this.money = money;
    }

    public static DisplayedItemInfo getDisplayedItemInfo(Connection connection, int itemKey) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, itemKey));
        
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }

        DisplayedItemInfo itemInfo = new DisplayedItemInfo(
            queryResult.getInt(1),
            queryResult.getInt(2),
            queryResult.getInt(3),
            queryResult.getInt(4),
            queryResult.getInt(5),
            queryResult.getInt(6),
            queryResult.getInt(7),
            queryResult.getInt(8),
            queryResult.getFloat(9),
            queryResult.getString(10).charAt(0),
            queryResult.getString(11).charAt(0),
            queryResult.getInt(12),
            queryResult.getString(13),
            queryResult.getString(14),
            queryResult.getInt(15),
            queryResult.getInt(16),
            queryResult.getInt(17),
            queryResult.getInt(18),
            queryResult.getInt(19),
            queryResult.getInt(20),
            queryResult.getString(21),
            queryResult.getInt(22)
        );

        statement.close();
        queryResult.close();

        return itemInfo;
    }
}
