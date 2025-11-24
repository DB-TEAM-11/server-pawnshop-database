package phase4.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase4.exceptions.NotASuchRowException;

public class PersonalDebt {
    private static final String QUERY = "SELECT PERSONAL_DEBT FROM PLAYER P, GAME_SESSION G WHERE P.PLAYER_KEY = G.PLAYER_KEY AND P.SESSION_TOKEN = '%s' AND G.GAME_END_DAY_COUNT IS NULL ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";
    private static final String UPDATE_QUERY = "UPDATE GAME_SESSION G SET G.PERSONAL_DEBT = G.PERSONAL_DEBT + %d WHERE G.PLAYER_KEY = (SELECT P.PLAYER_KEY FROM PLAYER P WHERE P.SESSION_TOKEN = '%s')";

    public static void addToPersonalDebt(Connection connection, String sessionToken, int amount) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_QUERY, amount, sessionToken));
        statement.close();
        return;
    }

    public static int getPersonalDebt(Connection connection, String sessionToken) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, sessionToken));
        
        int personalDebt;
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        } else {
            personalDebt = queryResult.getInt(1);
        }

        statement.close();
        queryResult.close();

        return personalDebt;
    }
}
