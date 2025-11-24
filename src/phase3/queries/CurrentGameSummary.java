package phase3.queries;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase3.exceptions.NotASuchRowException;

public class CurrentGameSummary {
    private static String QUERY = "SELECT GS.NICKNAME, GS.SHOP_NAME, GS.GAME_END_DAY_COUNT, GS.GAME_END_DATE FROM PLAYER P, GAME_SESSION GS WHERE P.SESSION_TOKEN = '%s' AND P.PLAYER_KEY = GS.PLAYER_KEY ORDER BY GS.GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";

    public String nickName;
    public String shopName;
    public int gameEndDayCount;
    public Date gameEndDate;

    private CurrentGameSummary(String nickName, String shopName, int gameEndDayCount, Date gameEndDate) {
        this.nickName = nickName;
        this.shopName = shopName;
        this.gameEndDayCount = gameEndDayCount;
        this.gameEndDate = gameEndDate;
    }

    public static CurrentGameSummary retrieveGameSummary(Connection connection, String sessionToken) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet record = statement.executeQuery(String.format(QUERY, sessionToken));
        if (!record.next()) {
            throw new NotASuchRowException();
        }

        CurrentGameSummary summary = new CurrentGameSummary(
            record.getString(1),
            record.getString(2),
            record.getInt(3),
            record.getDate(4)
        );

        record.close();
        statement.close();
        return summary;
    }
}
