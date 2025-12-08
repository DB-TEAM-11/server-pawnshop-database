package phase4.queries;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import phase4.exceptions.NotASuchRowException;

public class WorldRecord {
    private static final String QUERY = "SELECT * FROM (SELECT p.player_id, gs.nickname, gs.shop_name, gs.game_end_day_count, gs.game_end_date FROM PLAYER P, GAME_SESSION GS WHERE p.player_key = gs.player_key AND gs.game_end_day_count > 0 ORDER BY gs.game_end_day_count ASC) WHERE ROWNUM <= 10";

    public String playerId;
    public String nickName;
    public String shopName;
    public int gameEndDayCount;
    public Date gameEndDate;

    private WorldRecord(String playerId, String nickName, String shopName, int gameEndDayCount, Date gameEndDate) {
        this.playerId = playerId;
        this.nickName = nickName;
        this.shopName = shopName;
        this.gameEndDayCount = gameEndDayCount;
        this.gameEndDate = gameEndDate;
    }

    public static WorldRecord[] retrieveWorldRecord(Connection connection) throws SQLException {
        try (
            Statement statement = connection.createStatement();
            ResultSet queryResult = statement.executeQuery(QUERY)
        ) {
            if (!queryResult.next()) {
                throw new NotASuchRowException();
            }
            ArrayList<WorldRecord> records = new ArrayList<WorldRecord>();
            do {
                records.add(new WorldRecord(
                    queryResult.getString(1),
                    queryResult.getString(2),
                    queryResult.getString(3),
                    queryResult.getInt(4),
                    queryResult.getDate(5)
                ));
            } while (queryResult.next());
            return records.toArray(new WorldRecord[0]);
        }
    }
}
