package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class MoneyUpdater {
    private static final String UPDATE_ADD_QUERY = "UPDATE GAME_SESSION SET MONEY = MONEY + ? WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = ? ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";
    private static final String UPDATE_SUBTRACT_QUERY = "UPDATE GAME_SESSION SET MONEY = MONEY - ? WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = ? ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";
    private static final String GET_MONEY_QUERY = "SELECT MONEY FROM GAME_SESSION WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = ? ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";

    public static void addMoney(Connection connection, int playerKey, int amount) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_ADD_QUERY)) {
            statement.setInt(1, amount);
            statement.setInt(2, playerKey);
            statement.executeUpdate();
        }
    }

    public static void subtractMoney(Connection connection, int playerKey, int amount) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SUBTRACT_QUERY)) {
            statement.setInt(1, amount);
            statement.setInt(2, playerKey);
            statement.executeUpdate();
        }
    }

    public static int getMoney(Connection connection, int playerKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_MONEY_QUERY)) {
            statement.setInt(1, playerKey);
            var queryResult = statement.executeQuery();
            if (queryResult.next()) {
                return queryResult.getInt(1);
            } else {
                throw new NotASuchRowException();
            }
        }
    }
}
