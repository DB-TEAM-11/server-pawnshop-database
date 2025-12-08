package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import phase4.exceptions.NotASuchRowException;

public class RandomCustomers {
    private static final String QUERY = "SELECT CUSTOMER_KEY FROM CUSTOMER_CATALOG ORDER BY DBMS_RANDOM.VALUE FETCH FIRST ? ROWS ONLY";
    
    public static int[] getRandomCustomers(Connection connection, int count) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, count);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                ArrayList<Integer> customerKeys = new ArrayList<Integer>();
                do {
                    customerKeys.add(queryResult.getInt(1));
                } while (queryResult.next());
                return customerKeys.stream().mapToInt(i -> i).toArray();
            }
        }
    }
}
