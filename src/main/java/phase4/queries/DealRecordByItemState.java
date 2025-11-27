package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import phase4.constants.ItemState;
import phase4.exceptions.NotASuchRowException;

public class DealRecordByItemState {
    private static final String QUERY = "SELECT DR.* FROM DEAL_RECORD DR, EXISTING_ITEM I WHERE DR.GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY) AND DR.ITEM_KEY = I.ITEM_KEY AND I.ITEM_STATE = %d ORDER BY DR.DRC_KEY";
    
    public int drcKey;
    public int gameSessionKey;
    public int sellerKey;
    public int buyerKey;
    public int itemKey;
    public int askingPrice;
    public int purchasePrice;
    public int appraisedPrice;
    public int sellingPrice;
    public int boughtDate;
    public int soldDate;
    public int lastActionDate;
    
    private DealRecordByItemState(
        int drcKey,
        int gameSessionKey,
        int sellerKey,
        int buyerKey,
        int itemKey,
        int askingPrice,
        int purchasePrice,
        int appraisedPrice,
        int sellingPrice,
        int boughtDate,
        int soldDate,
        int lastActionDate
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
    
    public static DealRecordByItemState[] getDealRecordByItemState(Connection connection, int playerId, ItemState itemState) throws SQLException {
        ArrayList<DealRecordByItemState> dealRecords = new ArrayList<DealRecordByItemState>();
        
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, playerId);
            statement.setInt(2, itemState.value());
            try (ResultSet queryResult = statement.executeQuery()) {
                while (queryResult.next()) {
                    dealRecords.add(new DealRecordByItemState(
                        queryResult.getInt(1),
                        queryResult.getInt(2),
                        queryResult.getInt(3),
                        queryResult.getInt(4),
                        queryResult.getInt(5),
                        queryResult.getInt(6),
                        queryResult.getInt(7),
                        queryResult.getInt(8),
                        queryResult.getInt(9),
                        queryResult.getInt(10),
                        queryResult.getInt(11),
                        queryResult.getInt(12)
                    ));
                }
            }
        }
        
        if (dealRecords.isEmpty()) {
            throw new NotASuchRowException();
        }
        return dealRecords.toArray(new DealRecordByItemState[0]);
    }
}
