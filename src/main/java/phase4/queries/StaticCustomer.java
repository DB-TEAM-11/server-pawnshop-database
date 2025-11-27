package phase4.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import phase4.exceptions.NotASuchRowException;

public class StaticCustomer {
    private static final String QUERY = "SELECT "
    		+ "CC.CUSTOMER_KEY, "
    		+ "CC.CUSTOMER_NAME, "
    		+ "IC.CATEGORY_NAME, "
    		+ "CC.IMG_ID "
    		+ "FROM CUSTOMER_CATALOG CC "
    		+ "JOIN ITEM_CATEGORY IC "
    		+ "ON CC.CATEGORY_KEY = IC.CATEGORY_KEY";

    public static StaticCustomer[] customerCatalog;

    public int customerKey;
    public String customerName;
    public String categoryName;
    public String imgId;


    private StaticCustomer(int customerKey, String customerName, String categoryName, String imgId) {
        this.customerKey = customerKey;
        this.customerName = customerName;
        this.categoryName = categoryName;
        this.imgId = imgId;

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
                staticCustomers.getString(3),
                staticCustomers.getString(4)
            ));
        } while (staticCustomers.next());

        statement.close();
        staticCustomers.close();

        customerCatalog = catalog.toArray(new StaticCustomer[0]);
        return customerCatalog;
    }
}
