package phase4.queries;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class CurrentGameSummary {
    private static String QUERY = "SELECT GS.NICKNAME, GS.SHOP_NAME, GS.GAME_END_DAY_COUNT, GS.GAME_END_DATE FROM PLAYER P, GAME_SESSION GS WHERE P.SESSION_TOKEN = ? AND P.PLAYER_KEY = GS.PLAYER_KEY ORDER BY GS.GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";

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
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setString(1, sessionToken);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                return new CurrentGameSummary(
                    queryResult.getString(1),
                    queryResult.getString(2),
                    queryResult.getInt(3),
                    queryResult.getDate(4)
                );
            }
        }
    }
}
