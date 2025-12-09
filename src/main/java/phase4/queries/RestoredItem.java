package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class RestoredItem {
    private static final String QUERY = "SELECT I.ITEM_KEY, IC.CATEGORY_KEY, IC.ITEM_CATALOG_NAME, I.ITEM_STATE, I.FLAW_EA, I.FOUND_FLAW_EA, I.IS_AUTHENTICITY_FOUND, I.GRADE, I.AUTHENTICITY, DR.APPRAISED_PRICE, G.DAY_COUNT, DR.LAST_ACTION_DATE FROM EXISTING_ITEM I, ITEM_CATALOG IC, DEAL_RECORD DR, GAME_SESSION G WHERE I.ITEM_STATE = 2 AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY AND I.ITEM_KEY = DR.ITEM_KEY AND DR.GAME_SESSION_KEY = G.GAME_SESSION_KEY AND G.GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = ? ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY) AND G.DAY_COUNT - DR.LAST_ACTION_DATE >= 1";

    public int itemKey;
    public int itemCategory;
    public String itemName;
    public int itemState;
    public int flawEa;
    public int foundFlawEa;
    public boolean isAuthenticityFound;
    public int grade;
    public boolean authenticity;
    public int appraisedPrice;
    public int dayCount;
    public int lastActionDate;

    private RestoredItem(
        int itemKey,
        int itemCategory,
        String itemName,
        int itemState,
        int flawEa,
        int foundFlawEa,
        boolean isAuthenticityFound,
        int grade,
        boolean authenticity,
        int appraisedPrice,
        int dayCount,
        int lastActionDate
    ) {
        this.itemKey = itemKey;
        this.itemCategory = itemCategory;
        this.itemName = itemName;
        this.itemState = itemState;
        this.flawEa = flawEa;
        this.foundFlawEa = foundFlawEa;
        this.isAuthenticityFound = isAuthenticityFound;
        this.grade = grade;
        this.authenticity = authenticity;
        this.appraisedPrice = appraisedPrice;
        this.dayCount = dayCount;
        this.lastActionDate = lastActionDate;
    }

    public static RestoredItem[] getRestoredItem(Connection connection, int playerKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, playerKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                ArrayList<RestoredItem> restoringItems = new ArrayList<RestoredItem>();
                while (queryResult.next()) {
                    restoringItems.add(new RestoredItem(
                        queryResult.getInt(1),
                        queryResult.getInt(2),
                        queryResult.getString(3),
                        queryResult.getInt(4),
                        queryResult.getInt(5),
                        queryResult.getInt(6),
                        queryResult.getString(7).equals("Y"),
                        queryResult.getInt(8),
                        queryResult.getString(9).equals("Y"),
                        queryResult.getInt(10),
                        queryResult.getInt(11),
                        queryResult.getInt(12)
                    ));
                }
                return restoringItems.toArray(new RestoredItem[0]);
            }
        }
    }
}
