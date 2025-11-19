package phase3.queries;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.security.MessageDigest;
import java.security.SecureRandom;
import phase3.queries.HashedPwGetter;

public class SessionTokenBySign {

    private static final String QUERY =
            "SELECT P.SESSION_TOKEN FROM PLAYER P WHERE P.PLAYER_ID = '%s' AND P.HASHED_PW = '%s'";

    public static String SessionTokenBySign(
            Connection connection,
            String id,
            String pw,
            String hashed_pw
    ) {
        String session_token = null;

        try {
	        	String[] pwAndSalt = hashed_pw.split(";");
	          if (pwAndSalt.length < 2) return null;
	
	          String storedHash = pwAndSalt[0];
	          String salt = pwAndSalt[1];
	          
            String newHashedPw = HashedPwGetter.sha256(pw, salt);
                       
            if (!storedHash.equals(newHashedPw)) return null;

            Statement statement = connection.createStatement();

            String query = String.format(QUERY, id, hashed_pw);
            ResultSet rs = statement.executeQuery(query);

            if (rs.next()) {
                return rs.getString(1);
            }

            rs.close();
            statement.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    
    public static String getSalt16() {
      SecureRandom r = new SecureRandom();
      byte[] salt = new byte[8]; // 8바이트 * 2 hex = 16자리
      r.nextBytes(salt);

      StringBuilder sb = new StringBuilder();
      for (byte b : salt) {
          sb.append(String.format("%02x", b));
      }

      return sb.toString();
  }
}
