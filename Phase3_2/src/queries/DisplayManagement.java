package queries;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DisplayManagement {
    private static final String INSERT_DISPLAY_QUERY = "INSERT INTO GAME_SESSION_ITEM_DISPLAY (GAME_SESSION_KEY, DISPLAY_POS, ITEM_KEY) VALUES (%d, %d, %d)";
    
    private static final String DELETE_DISPLAY_QUERY = "DELETE FROM GAME_SESSION_ITEM_DISPLAY WHERE ITEM_KEY = %d";
    
    private static final String GET_DISPLAY_POSITIONS_QUERY = "SELECT DISPLAY_POS FROM GAME_SESSION_ITEM_DISPLAY WHERE GAME_SESSION_KEY = (SELECT GAME_SESSION_KEY FROM GAME_SESSION WHERE PLAYER_KEY = (SELECT PLAYER_KEY FROM PLAYER WHERE SESSION_TOKEN = '%s') ORDER BY GAME_SESSION_KEY DESC FETCH FIRST ROW ONLY) ORDER BY DISPLAY_POS ASC";

    public static void addToDisplay(Connection connection, int gameSessionKey, int displayPos, int itemKey) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(INSERT_DISPLAY_QUERY, gameSessionKey, displayPos, itemKey));
        statement.close();
    }

    public static void removeFromDisplay(Connection connection, int itemKey) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(DELETE_DISPLAY_QUERY, itemKey));
        statement.close();
    }

    public static int[] getUsedDisplayPositions(Connection connection, String sessionToken) throws SQLException {
        Statement statement = connection.createStatement();
        var queryResult = statement.executeQuery(String.format(GET_DISPLAY_POSITIONS_QUERY, sessionToken));
        
        java.util.ArrayList<Integer> positions = new java.util.ArrayList<Integer>();
        while (queryResult.next()) {
            positions.add(queryResult.getInt(1));
        }

        statement.close();
        queryResult.close();

        return positions.stream().mapToInt(i -> i).toArray();
    }
}
