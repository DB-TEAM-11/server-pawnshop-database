package phase3.queries;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class WorldRecord {
    private static String QUERY = "SELECT * FROM (SELECT p.player_id, gs.nickname, gs.shop_name, gs.game_end_day_count, gs.game_end_date FROM PLAYER P, GAME_SESSION GS WHERE p.player_key = gs.player_key AND gs.game_end_day_count > 0 ORDER BY gs.game_end_day_count ASC) WHERE ROWNUM <= 10";

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
        Statement statement = connection.createStatement();
        ResultSet record = statement.executeQuery(QUERY);
        if (!record.next()) {
            return null;
        }

        ArrayList<WorldRecord> records = new ArrayList<WorldRecord>();
        do {
            records.add(new WorldRecord(
                record.getString(1),
                record.getString(2),
                record.getString(3),
                record.getInt(4),
                record.getDate(5)
            ));
        } while (record.next());

        record.close();
        statement.close();
        return records.toArray(new WorldRecord[0]);
    }
}
