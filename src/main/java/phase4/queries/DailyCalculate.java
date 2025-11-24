package phase4.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase4.exceptions.NotASuchRowException;

public class DailyCalculate {
    private static final String QUERY = "SELECT G.MONEY + SUM(BOUGHT.PURCHASE_PRICE) - SUM(SOLD.SELLING_PRICE) AS TODAY_START, G.MONEY AS TODAY_END, FLOOR(G.PAWNSHOP_DEBT * 0.05) AS TODAY_INTEREST, G.MONEY - FLOOR(G.PAWNSHOP_DEBT * 0.05) AS TODAY_FINAL FROM (( GAME_SESSION G LEFT OUTER JOIN DEAL_RECORD BOUGHT ON G.GAME_SESSION_KEY = BOUGHT.GAME_SESSION_KEY AND G.DAY_COUNT = BOUGHT.BOUGHT_DATE ) LEFT OUTER JOIN DEAL_RECORD SOLD ON G.GAME_SESSION_KEY = SOLD.GAME_SESSION_KEY AND G.DAY_COUNT = SOLD.SOLD_DATE ) WHERE G.GAME_SESSION_KEY = ( SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY ) GROUP BY G.MONEY, G.PAWNSHOP_DEBT";

    public int todayStart;
    public int todayEnd;
    public int todayInterest;
    public int todayFinal;

    private DailyCalculate(
        int todayStart,
        int todayEnd,
        int todayInterest,
        int todayFinal
    ) {
        this.todayStart = todayStart;
        this.todayEnd = todayEnd;
        this.todayInterest = todayInterest;
        this.todayFinal = todayFinal;
    }

    public static DailyCalculate getDailyCalculate(Connection connection, int playerId) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, playerId));
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }

        DailyCalculate dailyCalculate = new DailyCalculate(
            queryResult.getObject(1) == null ? queryResult.getInt(2) : queryResult.getInt(1),
            queryResult.getInt(2),
            queryResult.getInt(3),
            queryResult.getInt(4)
        );

        statement.close();
        queryResult.close();

        return dailyCalculate;
    }
}
