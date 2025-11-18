package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import phase3.exceptions.NotASuchRowException;

public class DealRecordByItemState {
    private static final String QUERY = "SELECT DR.* FROM DEAL_RECORD DR, EXISTING_ITEM I WHERE DR.GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY) AND DR.ITEM_KEY = I.ITEM_KEY AND I.ITEM_STATE = %d ORDER BY DR.DRC_KEY";

    public int drcKey;
    public int gameSessionKey;
    public int sellerKey;
    public Integer buyerKey;
    public int itemKey;
    public Integer askingPrice;
    public Integer purchasePrice;
    public Integer appraisedPrice;
    public Integer sellingPrice;
    public Integer boughtDate;
    public Integer soldDate;
    public Integer lastActionDate;

    private DealRecordByItemState(
        int drcKey,
        int gameSessionKey,
        int sellerKey,
        Integer buyerKey,
        int itemKey,
        Integer askingPrice,
        Integer purchasePrice,
        Integer appraisedPrice,
        Integer sellingPrice,
        Integer boughtDate,
        Integer soldDate,
        Integer lastActionDate
    ) {
        this.drcKey = drcKey;
        this.gameSessionKey = gameSessionKey;
        this.sellerKey = sellerKey;
        this.buyerKey = buyerKey;
        this.itemKey = itemKey;
        this.askingPrice = askingPrice;
        this.purchasePrice = purchasePrice;
        this.appraisedPrice = appraisedPrice;
        this.sellingPrice = sellingPrice;
        this.boughtDate = boughtDate;
        this.soldDate = soldDate;
        this.lastActionDate = lastActionDate;
    }

    public static DealRecordByItemState[] getDealRecordByItemState(Connection connection, int playerId, int itemState) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, playerId, itemState));
        
        ArrayList<DealRecordByItemState> dealRecords = new ArrayList<DealRecordByItemState>();
        while (queryResult.next()) {
            dealRecords.add(new DealRecordByItemState(
                queryResult.getInt(1),
                queryResult.getInt(2),
                queryResult.getInt(3),
                (Integer) queryResult.getObject(4),
                queryResult.getInt(5),
                (Integer) queryResult.getObject(6),
                (Integer) queryResult.getObject(7),
                (Integer) queryResult.getObject(8),
                (Integer) queryResult.getObject(9),
                (Integer) queryResult.getObject(10),
                (Integer) queryResult.getObject(11),
                (Integer) queryResult.getObject(12)
            ));
        }

        statement.close();
        queryResult.close();

        if (dealRecords.isEmpty()) {
            throw new NotASuchRowException();
        }

        return dealRecords.toArray(new DealRecordByItemState[0]);
    }
}
