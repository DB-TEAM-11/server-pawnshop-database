package phase4.queries;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase4.exceptions.NotASuchRowException;

public class PlayerInfo {
    private static final String QUERY = "SELECT P.PLAYER_ID, GS.* FROM PLAYER P JOIN GAME_SESSION GS ON P.PLAYER_KEY = GS.PLAYER_KEY WHERE P.PLAYER_KEY = %d ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";
    
    public String playerId;
    public int gameSessionKey;
    public int playerKey;
    public int dayCount;
    public int money;
    public int personalDebt;
    public int pawnshopDebt;
    public int unlockedShowcaseCount;
    public String nickname;
    public String shopName;
    public int gameEndDayCount;
    public Date gameEndDate;

    private PlayerInfo(
        String playerId,
        int gameSessionKey,
        int playerKey,
        int dayCount,
        int money,
        int personalDebt,
        int pawnshopDebt,
        int unlockedShowcaseCount,
        String nickname,
        String shopName,
        int gameEndDayCount,
        Date gameEndDate
    ) {
        this.playerId = playerId;
        this.gameSessionKey = gameSessionKey;
        this.playerKey = playerKey;
        this.dayCount = dayCount;
        this.money = money;
        this.personalDebt = personalDebt;
        this.pawnshopDebt = pawnshopDebt;
        this.unlockedShowcaseCount = unlockedShowcaseCount;
        this.nickname = nickname;
        this.shopName = shopName;
        this.gameEndDayCount = gameEndDayCount;
        this.gameEndDate = gameEndDate;
    }

    public static PlayerInfo getPlayerInfo(Connection connection, int playerId) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, playerId));
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }

        PlayerInfo playerInfo = new PlayerInfo(
            queryResult.getString(1),
            queryResult.getInt(2),
            queryResult.getInt(3),
            queryResult.getInt(4),
            queryResult.getInt(5),
            queryResult.getInt(6),
            queryResult.getInt(7),
            queryResult.getInt(8),
            queryResult.getString(9),
            queryResult.getString(10),
            queryResult.getInt(11),
            queryResult.getDate(12)
        );

        statement.close();
        queryResult.close();

        return playerInfo;
    }
}
