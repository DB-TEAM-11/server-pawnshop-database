package phase4.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnector {
    public static final String URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    public static final String USER_GAME = "MYGAME";
    public static final String USER_PASSWD = "GAME1234";
    
    public static Connection connect() throws SQLException{
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch(ClassNotFoundException e) {
            System.err.println("error = " + e.getMessage());
            System.exit(1);
        }
        
        Connection connection = DriverManager.getConnection(URL, USER_GAME, USER_PASSWD);
        return connection;
    }
}
