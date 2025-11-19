package queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import exceptions.NotASuchRowException;

public class RandomCustomers {
    private static final String QUERY = "SELECT CUSTOMER_KEY FROM CUSTOMER_CATALOG ORDER BY DBMS_RANDOM.VALUE FETCH FIRST %d ROWS ONLY";

    public static int[] getRandomCustomers(Connection connection, int count) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, count));
        
        ArrayList<Integer> customerKeys = new ArrayList<Integer>();
        while (queryResult.next()) {
            customerKeys.add(queryResult.getInt(1));
        }

        statement.close();
        queryResult.close();

        if (customerKeys.isEmpty()) {
            throw new NotASuchRowException();
        }

        return customerKeys.stream().mapToInt(i -> i).toArray();
    }
}
