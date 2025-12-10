package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import phase4.exceptions.NotASuchRowException;

public class RandomCustomerWithDetail {
    private static final String QUERY = "SELECT CUSTOMER_KEY, FRAUD, WELL_COLLECT, CLUMSY, CATEGORY_KEY FROM CUSTOMER_CATALOG ORDER BY DBMS_RANDOM.VALUE FETCH FIRST ? ROWS ONLY";

    public int customerKey;
    public float fraud;
    public float wellCollect;
    public float clumsy;
    public int categoryKey;

    private RandomCustomerWithDetail(int customerKey, float fraud, float wellCollect, float clumsy, int categoryKey) {
        this.customerKey = customerKey;
        this.fraud = fraud;
        this.wellCollect = wellCollect;
        this.clumsy = clumsy;
        this.categoryKey = categoryKey;
    }

    public static RandomCustomerWithDetail[] getRandomCustomersWithDetails(Connection connection, int count) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, count);
            try (ResultSet queryResult = statement.executeQuery();) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                ArrayList<RandomCustomerWithDetail> customers = new ArrayList<>();
                do {
                    customers.add(new RandomCustomerWithDetail(
                        queryResult.getInt(1),      // CUSTOMER_KEY
                        queryResult.getFloat(2),    // FRAUD
                        queryResult.getFloat(3),    // WELL_COLLECT
                        queryResult.getFloat(4),    // CLUMSY
                        queryResult.getInt(5)       // CATEGORY_KEY
                    ));
                } while (queryResult.next());
                return customers.toArray(new RandomCustomerWithDetail[0]);
            }
        }
    }
}
