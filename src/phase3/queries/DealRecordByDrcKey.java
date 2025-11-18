package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase3.exceptions.NotASuchRowException;

public class DealRecordByDrcKey {
    private static final String QUERY = "SELECT I.*, DR.* FROM EXISTING_ITEM I, DEAL_RECORD DR WHERE DR.DRC_KEY = %d AND DR.ITEM_KEY = I.ITEM_KEY";

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
    public int drcKey;
    public int sellerKey;
    public Integer buyerKey;
    public Integer askingPrice;
    public Integer purchasePrice;
    public Integer appraisedPrice;
    public Integer sellingPrice;
    public Integer boughtDate;
    public Integer soldDate;
    public Integer lastActionDate;

    private DealRecordByDrcKey(
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
        int drcKey,
        int gameSessionKeyFromDR,
        int sellerKey,
        Integer buyerKey,
        int itemKeyFromDR,
        Integer askingPrice,
        Integer purchasePrice,
        Integer appraisedPrice,
        Integer sellingPrice,
        Integer boughtDate,
        Integer soldDate,
        Integer lastActionDate
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
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, drcKey));
        
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }

        DealRecordByDrcKey dealRecord = new DealRecordByDrcKey(
            queryResult.getInt(1),
            queryResult.getInt(2),
            queryResult.getInt(3),
            queryResult.getInt(4),
            queryResult.getInt(5),
            queryResult.getInt(6),
            queryResult.getInt(7),
            queryResult.getFloat(8),
            queryResult.getString(9).charAt(0),
            queryResult.getString(10).charAt(0),
            queryResult.getInt(11),
            queryResult.getInt(12),
            queryResult.getInt(13),
            queryResult.getInt(14),
            (Integer) queryResult.getObject(15),
            queryResult.getInt(16),
            (Integer) queryResult.getObject(17),
            (Integer) queryResult.getObject(18),
            (Integer) queryResult.getObject(19),
            (Integer) queryResult.getObject(20),
            (Integer) queryResult.getObject(21),
            (Integer) queryResult.getObject(22),
            (Integer) queryResult.getObject(23)
        );

        statement.close();
        queryResult.close();

        return dealRecord;
    }
}
