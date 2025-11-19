package phase3.queries;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class HashedPwGetter {
  private static final String QUERY =
          "SELECT P.HASHED_PW FROM PLAYER P WHERE P.PLAYER_ID = '%s'";

  public static String GetHashedPw(Connection connection, String id) {
    String hashed_pw = null;

    try {
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery(String.format(QUERY, id));

      if (rs.next()) {
        hashed_pw = rs.getString(1);
      }

      rs.close();
      statement.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return hashed_pw;
  }
  
  public static String sha256(String pw, String salt) throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] hashed = md.digest((pw + salt).getBytes("UTF-8"));

    StringBuilder sb = new StringBuilder();
    for (byte b : hashed) {
        sb.append(String.format("%02x", b));
    }
    return sb.toString();
}
  
}
