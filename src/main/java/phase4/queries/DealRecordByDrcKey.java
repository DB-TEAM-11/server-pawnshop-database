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
        int gameSessionKeyFromDR,
        int sellerKey,
        int buyerKey,
        int itemKeyFromDR,
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
                    queryResult.getInt(1),
                    queryResult.getInt(2),
                    queryResult.getInt(3),
                    queryResult.getInt(4),
                    queryResult.getInt(5),
                    queryResult.getInt(6),
                    queryResult.getInt(7),
                    queryResult.getFloat(8),
                    queryResult.getString(9).equals("Y"),
                    queryResult.getString(10).equals("Y"),
                    queryResult.getInt(11),
                    queryResult.getInt(12),
                    queryResult.getInt(13),
                    queryResult.getInt(14),
                    queryResult.getInt(15),
                    queryResult.getInt(16),
                    queryResult.getInt(17),
                    queryResult.getInt(18),
                    queryResult.getInt(19),
                    queryResult.getInt(20),
                    queryResult.getInt(21),
                    queryResult.getInt(22),
                    queryResult.getInt(23)
                );
            }
        }
    }
}
