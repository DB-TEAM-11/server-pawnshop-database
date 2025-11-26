package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class PersonalDebt {
    private static final String QUERY = "SELECT PERSONAL_DEBT FROM GAME_SESSION G WHERE PLAYER_KEY = ? AND GAME_END_DAY_COUNT IS NULL ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY";
    private static final String UPDATE_QUERY = "UPDATE GAME_SESSION SET PERSONAL_DEBT = PERSONAL_DEBT + ? WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION G WHERE PLAYER_KEY = ? ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY)";

    public static void addToPersonalDebt(Connection connection, int playerKey, int amount) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_QUERY)) {
            statement.setInt(1, amount);
            statement.setInt(2, playerKey);
            statement.executeUpdate();
        }
        return;
    }

    public static int getPersonalDebt(Connection connection, int playerKey) throws SQLException {
        int personalDebt;
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, playerKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                } else {
                    personalDebt = queryResult.getInt(1);
                }
            }
        }
        return personalDebt;
    }
}
