package queries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateMoney {
    private static final String UPDATE_ADD_QUERY = "UPDATE GAME_SESSION SET MONEY = MONEY + %d WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";
    
    private static final String UPDATE_SUBTRACT_QUERY = "UPDATE GAME_SESSION SET MONEY = MONEY - %d WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";

    private static final String GET_MONEY_QUERY = "SELECT MONEY FROM GAME_SESSION WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";

    public static void addMoney(Connection connection, String sessionToken, int amount) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_ADD_QUERY, amount, sessionToken));
        statement.close();
    }

    public static void subtractMoney(Connection connection, String sessionToken, int amount) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(UPDATE_SUBTRACT_QUERY, amount, sessionToken));
        statement.close();
    }

    public static int getMoney(Connection connection, String sessionToken) throws SQLException {
        Statement statement = connection.createStatement();
        var queryResult = statement.executeQuery(String.format(GET_MONEY_QUERY, sessionToken));
        
        int money = 0;
        if (queryResult.next()) {
            money = queryResult.getInt(1);
        }

        statement.close();
        queryResult.close();

        return money;
    }
}
