package phase4.queries;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class CurrentGameSummary {
    private static String QUERY = "SELECT NICKNAME, SHOP_NAME, GAME_END_DAY_COUNT, GAME_END_DATE FROM GAME_SESSION WHERE GAME_SESSION_KEY = ?";

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

    public static CurrentGameSummary retrieveGameSummary(Connection connection, int gameSessionKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, gameSessionKey);
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
