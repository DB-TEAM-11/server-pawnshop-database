package phase3.queries;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuthenticationCreator {
	 private static final String QUERY = 
			 "INSERT INTO PLAYER P "
			 + "("
			 + "P.PLAYER_ID, "
			 + "P.HASHED_PW, "
			 + "P.SESSION_TOKEN, "
			 + "P.LAST_ACTIVITY"
			 + ")"
			 + " VALUES (?, ?, ?, ?)";
	 
	 public static void CreateAuthentication(
			 Connection connection,
			 String id,
			 String hashedPwWithSalt
	 ) {
		 
		 PreparedStatement statement;
		 try {
			 statement = connection.prepareStatement(QUERY);
			 
			 statement.setString(1, id);
			 statement.setString(2, hashedPwWithSalt);
			 statement.setString(3, MakeRandToken());
			 java.util.Date now = new java.util.Date();
			 java.sql.Date sqlDate = new java.sql.Date(now.getTime());
			 statement.setDate(4, sqlDate);			 
			 
			 statement.executeUpdate();
			 System.out.println("회원가입에 성공하였습니다. 로그인 기능으로 이동합니다.");
	     statement.close();
		 } catch (SQLException e) {
			e.printStackTrace();
		 }
	 }
	 
	 private static String MakeRandToken() {
		 SecureRandom random = new SecureRandom();
     byte[] bytes = new byte[32];  // 64바이트
     random.nextBytes(bytes);

     StringBuilder sb = new StringBuilder();
     for (byte b : bytes) {
         sb.append(String.format("%02x", b));
     }

     return sb.toString();
	 }
	 
	 public static boolean IsEnglishOnly(String str) {
	    return str.matches("[a-zA-Z]+");
	}
}
