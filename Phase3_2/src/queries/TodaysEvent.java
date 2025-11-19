package queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import exceptions.NotASuchRowException;

public class TodaysEvent {
    private static final String QUERY = "SELECT * FROM EXISTING_NEWS N, NEWS_CATALOG NC WHERE N.GAME_SESSION_KEY = ( SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY ) AND N.NCAT_KEY = NC.NCT_KEY ORDER BY NC.NCT_KEY";
    
    public int gameSessionKey;
    public int ncatKey;
    public int amount;
    public int nctKey;
    public String newsDescription;
    public int affectedPrice;
    public int categoryKey;
    public int plusMinus;

    private TodaysEvent(
        int gameSessionKey,
        int ncatKey,
        int amount,
        int nctKey,
        String newsDescription,
        int affectedPrice,
        int categoryKey,
        int plusMinus
    ) {
        this.gameSessionKey = gameSessionKey;
        this.ncatKey = ncatKey;
        this.amount = amount;
        this.nctKey = nctKey;
        this.newsDescription = newsDescription;
        this.affectedPrice = affectedPrice;
        this.categoryKey = categoryKey;
        this.plusMinus = plusMinus;
    }

    public static TodaysEvent[] getTodaysEvent(Connection connection, int playerId) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, playerId));
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }
        
        ArrayList<TodaysEvent> todaysEvent = new ArrayList<TodaysEvent>();
        do {
            todaysEvent.add(new TodaysEvent(
                queryResult.getInt(1),
                queryResult.getInt(2),
                queryResult.getInt(3),
                queryResult.getInt(4),
                queryResult.getString(5),
                queryResult.getInt(6),
                queryResult.getInt(7),
                queryResult.getInt(8)
            ));
        } while (queryResult.next());

        statement.close();
        queryResult.close();

        return todaysEvent.toArray(new TodaysEvent[0]);
    }
}
