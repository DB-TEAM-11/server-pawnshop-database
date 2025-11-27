package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class CustomerInfo {
    private static final String QUERY = "SELECT CUSTOMER_KEY, CUSTOMER_NAME, CATEGORY_KEY, IMG_ID, FRAUD, WELL_COLLECT, CLUMSY FROM CUSTOMER_CATALOG WHERE CUSTOMER_KEY = ?";

    public int customerKey;
    public String customerName;
    public int categoryKey;
    public String imgId;
    public float fraud;
    public float wellCollect;
    public float clumsy;

    private CustomerInfo(
        int customerKey,
        String customerName,
        int categoryKey,
        String imgId,
        float fraud,
        float wellCollect,
        float clumsy
    ) {
        this.customerKey = customerKey;
        this.customerName = customerName;
        this.categoryKey = categoryKey;
        this.imgId = imgId;
        this.fraud = fraud;
        this.wellCollect = wellCollect;
        this.clumsy = clumsy;
    }

    public static CustomerInfo getCustomerInfo(Connection connection, int customerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, customerId);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                return new CustomerInfo(
                    queryResult.getInt(1),
                    queryResult.getString(2),
                    queryResult.getInt(3),
                    queryResult.getString(4),
                    queryResult.getFloat(5),
                    queryResult.getFloat(6),
                    queryResult.getFloat(7)
                );
            }
        }
    }
}
