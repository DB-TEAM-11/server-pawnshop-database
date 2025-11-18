package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase3.exceptions.NotASuchRowException;

public class CustomerInfo {
    private static final String QUERY = "SELECT CUSTOMER_NAME, IMG_ID, FRAUD, WELL_COLLECT, CLUMSY FROM CUSTOMER_CATALOG WHERE CUSTOMER_KEY = %d";

    public String customerName;
    public String imgId;
    public float fraud;
    public float wellCollect;
    public float clumsy;

    private CustomerInfo(
        String customerName,
        String imgId,
        float fraud,
        float wellCollect,
        float clumsy
    ) {
        this.customerName = customerName;
        this.imgId = imgId;
        this.fraud = fraud;
        this.wellCollect = wellCollect;
        this.clumsy = clumsy;
    }

    public static CustomerInfo getCustomerInfo(Connection connection, int customerId) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, customerId));
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }

        CustomerInfo customerInfo = new CustomerInfo(
            queryResult.getString(1),
            queryResult.getString(2),
            queryResult.getFloat(3),
            queryResult.getFloat(4),
            queryResult.getFloat(5)
        );

        statement.close();
        queryResult.close();

        return customerInfo;
    }
}
