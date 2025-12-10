package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TodaysEvent { // 기존 쿼리
    private static final String QUERY = "SELECT NC.NEWS_DESCRIPTION, NC.AFFECTED_PRICE, NC.PLUS_MINUS, IC.CATEGORY_NAME, EN.AMOUNT, NC.CATEGORY_KEY FROM NEWS_CATALOG NC JOIN EXISTING_NEWS EN ON NC.NCT_KEY = EN.NCAT_KEY JOIN ITEM_CATEGORY IC ON NC.CATEGORY_KEY = IC.CATEGORY_KEY WHERE EN.GAME_SESSION_KEY = ?";
    
    public String newsDescription;
    public int affectedPrice;
    public int plusMinus;
    public String categoryName;
    public int categoryKey;
    public int amount;

    private TodaysEvent(
        String newsDescription,
        int affectedPrice,
        String categoryName,
        int amount,
        int categoryKey
    ) {
        this.newsDescription = newsDescription;
        this.affectedPrice = affectedPrice;
        this.categoryName = categoryName;
        this.categoryKey = categoryKey;
        this.amount = amount;
    }
    
    public static TodaysEvent[] getTodaysEvent(Connection connection, int gameSessionKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, gameSessionKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                ArrayList<TodaysEvent> todaysEvent = new ArrayList<TodaysEvent>();
                while (queryResult.next()) {
                    todaysEvent.add(new TodaysEvent(
                        queryResult.getString(1),
                        queryResult.getInt(2),
                        queryResult.getString(4),
                        queryResult.getInt(3) * queryResult.getInt(5),
                        queryResult.getInt(6)
                    ));
                }
                return todaysEvent.toArray(new TodaysEvent[0]);
            }
        }
    }
}
