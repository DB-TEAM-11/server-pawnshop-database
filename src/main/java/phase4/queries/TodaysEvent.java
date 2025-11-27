package phase4.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import phase4.exceptions.NotASuchRowException;

public class TodaysEvent { // 기존 쿼리
    private static final String QUERY = "SELECT "
    		+ "NC.NEW_DESCRIPTION, "
    		+ "NC.AFFECTED_PRICE, "
    		+ "NC.PLUS_MINUS, "
    		+ "IC.CATEGORY_NAME, "
    		+ "EN.AMOUNT, "
    		+ "FROM NEWS_CATALOG NC "
    		+ "JOIN EXISTING_NEWS EN "
    		+ "ON NC.NCT_KEY = EN.NCAT_KEY "
    		+ "JOIN ITEM_CATEGORY IC "
    		+ "ON NC.CATEGORY_KEY = IC.CATEGORY_KEY "
    		+ "WHERE EN.GAME_SESSION_KEY = %d";
    
    public String newsDescription;
    public int affectedPrice;
    public int plusMinus;
    public String categoryName;
    public int amount;


    private TodaysEvent(
        String newsDescription,
        int affectedPrice,
        String categoryName,
        int amount
    ) {
        this.newsDescription = newsDescription;
        this.affectedPrice = affectedPrice;
        this.categoryName = categoryName;
        this.amount = amount;

    }
    
    public static TodaysEvent[] getTodaysEvent(Connection connection, int gameSessionKey) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, gameSessionKey));
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }
        
        ArrayList<TodaysEvent> todaysEvent = new ArrayList<TodaysEvent>();
        do {
            todaysEvent.add(new TodaysEvent(
                queryResult.getString(1),
                queryResult.getInt(2),
                queryResult.getString(4),
                queryResult.getInt(3) * queryResult.getInt(5)
            ));
        } while (queryResult.next());

        statement.close();
        queryResult.close();

        return todaysEvent.toArray(new TodaysEvent[0]);
    }
}
