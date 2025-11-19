package queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class IdIsExist {
    private static final String QUERY = "SELECT HASHED_PW FROM PLAYER WHERE PLAYER_ID = '%s'";

    public static boolean isIdExist(Connection connection, String userId) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, userId));

        boolean result = queryResult.next();

        statement.close();
        queryResult.close();

        return result;
    }
}
