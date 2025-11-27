package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase4.exceptions.NotASuchRowException;

public class RevealCustomerInfo {
	private static final String QUERY = "SELECT "
	          + "CC.%d "
	          + "FROM CUSTOMER_CATALOG CC "
	          + "WHERE CC.CUSTOMER_KEY = %d";

	    public String attribute;
	    public float value;
	    public float leftMoney;

	    private RevealCustomerInfo(
	       String attribute,
	       float value,
	       int leftMoney
	    ) {
	        this.attribute = attribute;
	        this.value = value;
	        this.leftMoney = leftMoney;
	    }

	    public static RevealCustomerInfo getCustomerInfo(
	          Connection connection, 
	          int customerKey, 
	          String attribute,
	          int leftMoney
	       ) throws SQLException {
	       Statement statement = connection.createStatement();
	        ResultSet queryResult = statement.executeQuery(String.format(
	              QUERY, attribute, customerKey
	           ));      
	       
	        if (!queryResult.next()) {
	            throw new NotASuchRowException();
	        }
	        return new RevealCustomerInfo(
	        	attribute,
	            queryResult.getFloat(1),
	            leftMoney
	        );
	    }
}
