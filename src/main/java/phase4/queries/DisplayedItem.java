package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class DisplayedItem {
    private static final String RETRIEVE_QUERY = "SELECT D.DISPLAY_POS, I.*, IC.*, DR.DRC_KEY, DR.PURCHASE_PRICE, DR.ASKING_PRICE, DR.APPRAISED_PRICE, DR.BOUGHT_DATE, CC.CUSTOMER_NAME, GS.MONEY FROM GAME_SESSION_ITEM_DISPLAY D, EXISTING_ITEM I, ITEM_CATALOG IC, DEAL_RECORD DR, CUSTOMER_CATALOG CC, GAME_SESSION GS WHERE D.ITEM_KEY = ? AND D.ITEM_KEY = I.ITEM_KEY AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY AND I.ITEM_KEY = DR.ITEM_KEY AND DR.SELLER_KEY = CC.CUSTOMER_KEY AND GS.GAME_SESSION_KEY = DR.GAME_SESSION_KEY";
    private static final String DELETE_QUERY = "DELETE FROM GAME_SESSION_ITEM_DISPLAY WHERE ITEM_KEY = ?";

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
    public int dealRecordKey;
    public int purchasePrice;
    public int askingPrice;
    public int appraisedPrice;
    public int boughtDate;
    public String customerName;
    public int money;

    private DisplayedItem(
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
        int basePrice,
        int dealRecordKey,
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
        this.dealRecordKey = dealRecordKey;
        this.purchasePrice = purchasePrice;
        this.askingPrice = askingPrice;
        this.appraisedPrice = appraisedPrice;
        this.boughtDate = boughtDate;
        this.customerName = customerName;
        this.money = money;
    }

    public static DisplayedItem getDisplayedItem(Connection connection, int itemKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(RETRIEVE_QUERY)) {
            statement.setInt(1, itemKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                return new DisplayedItem(
                    queryResult.getInt(1),   // DISPLAY_POS
                    queryResult.getInt(2),   // ITEM_KEY
                    queryResult.getInt(3),   // GAME_SESSION_KEY
                    queryResult.getInt(4),   // ITEM_CATALOG_KEY
                    queryResult.getInt(5),   // GRADE
                    queryResult.getInt(6),   // FOUND_GRADE
                    queryResult.getInt(7),   // FLAW_EA
                    queryResult.getInt(8),   // FOUND_FLAW_EA
                    queryResult.getFloat(9), // SUSPICIOUS_FLAW_AURA
                    queryResult.getString(10).equals("Y"), // AUTHENTICITY (A/Y/N 문자)
                    queryResult.getString(11).equals("Y"), // IS_AUTHENTICITY_FOUND (I 문자는 'Y'가 아님)
                    queryResult.getInt(12),  // ITEM_STATE
                    queryResult.getString(14), // ITEM_CATALOG_NAME (IC.* 두번째 컬럼)
                    queryResult.getString(15), // IMG_ID (IC.* 세번째 컬럼)
                    queryResult.getInt(16),  // CATEGORY_KEY (IC.* 네번째 컬럼)
                    queryResult.getInt(17),  // BASE_PRICE (IC.* 다섯번째 컬럼)
                    queryResult.getInt(18),  // DRC_KEY
                    queryResult.getInt(19),  // PURCHASE_PRICE
                    queryResult.getInt(20),  // ASKING_PRICE
                    queryResult.getInt(21),  // APPRAISED_PRICE
                    queryResult.getInt(22),  // BOUGHT_DATE
                    queryResult.getString(23), // CUSTOMER_NAME (CUS 부분)
                    queryResult.getInt(24)   // MONEY
                );
            }
        }
    }

    public static void deleteDisplayedItemEntry(Connection connection, int itemKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_QUERY)) {
            statement.setInt(1, itemKey);
            statement.executeUpdate();
        }
    }
}
