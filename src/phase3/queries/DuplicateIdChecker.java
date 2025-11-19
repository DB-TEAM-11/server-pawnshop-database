package phase3.queries;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class DuplicateIdChecker {
	 private static final String QUERY =
       "SELECT P.PLAYER_ID FROM PLAYER P WHERE P.PLAYER_ID = '%s'";
	 
	 public static boolean CheckDuplicateId(
       Connection connection,
			 String id
   ) {
		 
		 try {
       Statement statement = connection.createStatement();
       String query = String.format(QUERY, id);
       ResultSet rs = statement.executeQuery(query);
       
       if (rs.next()) {
    		 return true;
       }       
		 } catch (Exception e) {
       e.printStackTrace();
		 }
		 return false;
	 }
}
