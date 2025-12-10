package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DisplayManagement {
    private static final String INSERT_DISPLAY_QUERY = "INSERT INTO GAME_SESSION_ITEM_DISPLAY (GAME_SESSION_KEY, DISPLAY_POS, ITEM_KEY) VALUES (?, ?, ?)";
    private static final String DELETE_DISPLAY_QUERY = "DELETE FROM GAME_SESSION_ITEM_DISPLAY WHERE ITEM_KEY = ?";
    private static final String GET_DISPLAY_POSITIONS_QUERY = "SELECT DISPLAY_POS FROM GAME_SESSION_ITEM_DISPLAY WHERE GAME_SESSION_KEY = ? ORDER BY DISPLAY_POS";

    public static void addToDisplay(Connection connection, int gameSessionKey, int displayPos, int itemKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_DISPLAY_QUERY)) {
            statement.setInt(1, gameSessionKey);
            statement.setInt(2, displayPos);
            statement.setInt(3, itemKey);
            statement.executeUpdate();
        }
    }

    public static void removeFromDisplay(Connection connection, int itemKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_DISPLAY_QUERY)) {
            statement.setInt(1, itemKey);
            statement.executeUpdate();
        }
    }

    public static int[] getUsedDisplayPositions(Connection connection, int gameSessionKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(GET_DISPLAY_POSITIONS_QUERY)) {
            statement.setInt(1, gameSessionKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                java.util.ArrayList<Integer> positions = new java.util.ArrayList<Integer>();
                while (queryResult.next()) {
                    positions.add(queryResult.getInt(1));
                }
                return positions.stream().mapToInt(i -> i).toArray();
            }
        }
    }
}
