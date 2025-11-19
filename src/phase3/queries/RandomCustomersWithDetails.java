package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import phase3.exceptions.NotASuchRowException;

public class RandomCustomersWithDetails {
    private static final String QUERY = "SELECT CUSTOMER_KEY, FRAUD, WELL_COLLECT, CLUMSY, CATEGORY_KEY FROM CUSTOMER_CATALOG ORDER BY DBMS_RANDOM.VALUE FETCH FIRST %d ROWS ONLY";

    public int customerKey;
    public float fraud;
    public float wellCollect;
    public float clumsy;
    public int categoryKey;

    private RandomCustomersWithDetails(int customerKey, float fraud, float wellCollect, float clumsy, int categoryKey) {
        this.customerKey = customerKey;
        this.fraud = fraud;
        this.wellCollect = wellCollect;
        this.clumsy = clumsy;
        this.categoryKey = categoryKey;
    }

    public static RandomCustomersWithDetails[] getRandomCustomersWithDetails(Connection connection, int count) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(String.format(QUERY, count));
        
        ArrayList<RandomCustomersWithDetails> customers = new ArrayList<RandomCustomersWithDetails>();
        while (rs.next()) {
            customers.add(new RandomCustomersWithDetails(
                rs.getInt(1),      // CUSTOMER_KEY
                rs.getFloat(2),    // FRAUD
                rs.getFloat(3),    // WELL_COLLECT
                rs.getFloat(4),    // CLUMSY
                rs.getInt(5)       // CATEGORY_KEY
            ));
        }

        statement.close();
        rs.close();

        if (customers.isEmpty()) {
            throw new NotASuchRowException();
        }

        return customers.toArray(new RandomCustomersWithDetails[0]);
    }
}
