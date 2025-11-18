package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase3.exceptions.NotASuchRowException;

public class PawnshopDebt {
    private static final String QUERY = "SELECT PAWNSHOP_DEBT FROM PLAYER P, GAME_SESSION G WHERE P.PLAYER_KEY = G.PLAYER_KEY AND P.PLAYER_KEY = 1 AND G.GAME_END_DAY_COUNT IS NULL ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";
    private static final String UPDATE_QUERY = "UPDATE GAME_SESSION G SET G.PAWNSHOP_DEBT = G.PAWNSHOP_DEBT + %d WHERE G.PLAYER_KEY = (SELECT P.PLAYER_KEY FROM PLAYER P WHERE P.SESSION_TOKEN = %s)";

    public static void addToShopDebt(Connection connection, String sessionToken, int amount) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_QUERY, amount, sessionToken));
        statement.close();
        return;
    }

    public static int getShopDebt(Connection connection, String sessionToken) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, sessionToken));
        
        int shopDebt;
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        } else {
            shopDebt = queryResult.getInt(1);
        }

        statement.close();
        queryResult.close();

        return shopDebt;
    }
}
