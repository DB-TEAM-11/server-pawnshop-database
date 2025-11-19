package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class RestoringItems {
    private static final String QUERY = "SELECT I.ITEM_KEY, IC.CATEGORY_KEY, IC.ITEM_CATALOG_NAME, I.ITEM_STATE, I.FLAW_EA, I.FOUND_FLAW_EA, I.IS_AUTHENTICITY_FOUND, I.GRADE, I.AUTHENTICITY, DR.APPRAISED_PRICE, G.DAY_COUNT, DR.LAST_ACTION_DATE FROM EXISTING_ITEM I, ITEM_CATALOG IC, DEAL_RECORD DR, GAME_SESSION G WHERE I.ITEM_STATE = 2 AND I.ITEM_CATALOG_KEY = IC.ITEM_CATALOG_KEY AND I.ITEM_KEY = DR.ITEM_KEY AND DR.GAME_SESSION_KEY = G.GAME_SESSION_KEY AND G.PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') AND G.GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY) AND G.DAY_COUNT - DR.LAST_ACTION_DATE >= 1";

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

    private RestoringItems(
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

    public static RestoringItems[] getRestoringItems(Connection connection, String sessionToken) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, sessionToken, sessionToken));
        
        ArrayList<RestoringItems> restoringItems = new ArrayList<RestoringItems>();
        while (queryResult.next()) {
            restoringItems.add(new RestoringItems(
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

        statement.close();
        queryResult.close();

        return restoringItems.toArray(new RestoringItems[0]);
    }
}
