package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AuctionedItem {
    private static final String QUERY = "SELECT I.ITEM_KEY, IC.CATEGORY_KEY, IC.ITEM_CATALOG_NAME, I.ITEM_STATE, DR.APPRAISED_PRICE, DR.ASKING_PRICE, G.DAY_COUNT, DR.LAST_ACTION_DATE FROM EXISTING_ITEM I, ITEM_CATALOG IC, DEAL_RECORD DR, GAME_SESSION G WHERE I.ITEM_STATE = 3 AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY AND I.ITEM_KEY = DR.ITEM_KEY AND DR.GAME_SESSION_KEY = G.GAME_SESSION_KEY AND G.GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = ? ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY) AND G.DAY_COUNT - DR.LAST_ACTION_DATE >= 2";

    public int itemKey;
    public int itemCategory;
    public String itemName;
    public int itemState;
    public int appraisedPrice;
    public int askingPrice;
    public int dayCount;
    public int lastActionDate;

    private AuctionedItem(
        int itemKey,
        int itemCategory,
        String itemName,
        int itemState,
        int appraisedPrice,
        int askingPrice,
        int dayCount,
        int lastActionDate
    ) {
        this.itemKey = itemKey;
        this.itemCategory = itemCategory;
        this.itemName = itemName;
        this.itemState = itemState;
        this.appraisedPrice = appraisedPrice;
        this.askingPrice = askingPrice;
        this.dayCount = dayCount;
        this.lastActionDate = lastActionDate;
    }

    public static AuctionedItem[] getAuctionedItems(Connection connection, int playerKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, playerKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                ArrayList<AuctionedItem> auctioningItems = new ArrayList<AuctionedItem>();
                while (queryResult.next()) {
                    auctioningItems.add(new AuctionedItem(
                        queryResult.getInt(1),
                        queryResult.getInt(2),
                        queryResult.getString(3),
                        queryResult.getInt(4),
                        queryResult.getInt(5),
                        queryResult.getInt(6),
                        queryResult.getInt(7),
                        queryResult.getInt(8)
                    ));
                }
                return auctioningItems.toArray(new AuctionedItem[0]);
            }
        }
    }
}
