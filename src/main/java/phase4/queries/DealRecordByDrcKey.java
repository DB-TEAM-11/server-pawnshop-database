package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class DealRecordByDrcKey {
    private static final String QUERY = "SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR WHERE DR.DRC_KEY = ? AND DR.ITEM_KEY = I.ITEM_KEY";

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
    public int drcKey;
    public int sellerKey;
    public int buyerKey;
    public int askingPrice;
    public int purchasePrice;
    public int appraisedPrice;
    public int sellingPrice;
    public int boughtDate;
    public int soldDate;
    public int lastActionDate;

    private DealRecordByDrcKey(
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
        int drcKey,
        int sellerKey,
        int buyerKey,
        int askingPrice,
        int purchasePrice,
        int appraisedPrice,
        int sellingPrice,
        int boughtDate,
        int soldDate,
        int lastActionDate
    ) {
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
        this.drcKey = drcKey;
        this.sellerKey = sellerKey;
        this.buyerKey = buyerKey;
        this.askingPrice = askingPrice;
        this.purchasePrice = purchasePrice;
        this.appraisedPrice = appraisedPrice;
        this.sellingPrice = sellingPrice;
        this.boughtDate = boughtDate;
        this.soldDate = soldDate;
        this.lastActionDate = lastActionDate;
    }

    public static DealRecordByDrcKey getDealRecordByDrcKey(Connection connection, int drcKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, drcKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
        
                return new DealRecordByDrcKey(
                    queryResult.getInt(1), //  ITEM_KEY 
                    queryResult.getInt(2), //  GAME_SESSION_KEY 
                    queryResult.getInt(3), //  ITEM_CATALOG_KEY 
                    queryResult.getInt(4), //  GRADE 
                    queryResult.getInt(5), // FOUND_GRADE 
                    queryResult.getInt(6), // FLAW_EA 
                    queryResult.getInt(7), //  FOUND_FLAW_EA 
                    queryResult.getFloat(8), //  SUSPICIOUS_FLAW_AURA 
                    queryResult.getString(9).equals("Y"), //  AUTHENTICITY 
                    queryResult.getString(10).equals("Y"), //  IS_AUTHENTICITY_FOUND 
                    queryResult.getInt(11), //  ITEM_STATE 
                    queryResult.getInt(12), //  DRC_KEY 
                    queryResult.getInt(14), //  SELLER_KEY 
                    queryResult.getInt(15), //  BUYER_KEY 
                    queryResult.getInt(17), //  ASKING_PRICE 
                    queryResult.getInt(18), //  PURCHASE_PRICE 
                    queryResult.getInt(19), //  APPRAISED_PRICE 
                    queryResult.getInt(20), //  SELLING_PRICE 
                    queryResult.getInt(21), //  BOUGHT_DATE 
                    queryResult.getInt(22), //  SOLD_DATE 
                    queryResult.getInt(23) //  LAST_ACTION_DATE 
                );
            }
        }
    }
}
