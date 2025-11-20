package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import phase3.exceptions.NotASuchRowException;

public class StaticCustomer {
    private static final String QUERY = "SELECT * FROM CUSTOMER_CATALOG";

    public static StaticCustomer[] customerCatalog;

    public int customerKey;
    public String customerName;
    public int categoryKey;
    public String imgId;
    public float fraud;
    public float wellCollect;
    public float clumsy;

    private StaticCustomer(int customerKey, String customerName, int categoryKey, String imgId, float fraud, float wellCollect, float clumsy) {
        this.customerKey = customerKey;
        this.customerName = customerName;
        this.categoryKey = categoryKey;
        this.imgId = imgId;
        this.fraud = fraud;
        this.wellCollect = wellCollect;
        this.clumsy = clumsy;
    }

    public static StaticCustomer[] loadAllCustomers(Connection connection) throws SQLException {
        if (customerCatalog != null) {
            return customerCatalog;
        }
        Statement statement = connection.createStatement();
        ResultSet staticCustomers = statement.executeQuery(QUERY);
        if (!staticCustomers.next()) {
            throw new NotASuchRowException();
        }
        
        ArrayList<StaticCustomer> catalog = new ArrayList<StaticCustomer>();
        do {
            catalog.add(new StaticCustomer(
                staticCustomers.getInt(1),
                staticCustomers.getString(2),
                staticCustomers.getInt(3),
                staticCustomers.getString(4),
                staticCustomers.getFloat(5),
                staticCustomers.getFloat(6),
                staticCustomers.getFloat(7)
            ));
        } while (staticCustomers.next());

        statement.close();
        staticCustomers.close();

        customerCatalog = catalog.toArray(new StaticCustomer[0]);
        return customerCatalog;
    }
}
