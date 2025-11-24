package phase4.utils;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import phase4.exceptions.CloseGameException;

public class SessionToken {
    private static final String VERIFY_QUERY = "SELECT P.PLAYER_KEY FROM PLAYER P WHERE P.PLAYER_ID = '%s' AND P.HASHED_PW = '%s'";
    private static final String UPDATE_SESSION_TOKEN_QUERY = "UPDATE PLAYER SET SESSION_TOKEN = '%s', LAST_ACTIVITY = TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS') WHERE PLAYER_ID = '%s'";

    public static String getSessionTokenByCredentials(Connection connection, String id, String pw, String hashed_pw) {
        String[] pwAndSalt = hashed_pw.split(";");
        if (pwAndSalt.length < 2) return null;

        String storedHash = pwAndSalt[0];
        String salt = pwAndSalt[1];
          
        String newHashedPw = HashedPwGetter.sha256(pw, salt);
                   
        if (!storedHash.equals(newHashedPw)) return null;

        Statement statement;
        try {
            statement = connection.createStatement();

            // 사용자 인증 확인
            String verifyQuery = String.format(VERIFY_QUERY, id, hashed_pw);
            ResultSet rs = statement.executeQuery(verifyQuery);

            if (!rs.next()) {
                rs.close();
                statement.close();
                return null;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }

        // 새 세션 토큰 생성 (Base64 64바이트)
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[48]; // 48바이트를 Base64 인코딩하면 64자리
        random.nextBytes(tokenBytes);
        String newSessionToken = Base64.getEncoder().encodeToString(tokenBytes);
        
        // 현재 시간
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String datetime = now.format(formatter);
        
        // 세션 토큰 업데이트
        String updateQuery = String.format(UPDATE_SESSION_TOKEN_QUERY, newSessionToken, datetime, id);
        try {
            statement.executeUpdate(updateQuery);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }

        return newSessionToken;
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
