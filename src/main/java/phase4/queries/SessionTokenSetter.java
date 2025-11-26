package phase4.queries;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import phase4.exceptions.NotASuchRowException;
import phase4.utils.PasswordHasher;

public class SessionTokenSetter {
    private static final String QUERY_GET_HASHEDPW = "SELECT P.HASHED_PW FROM PLAYER P WHERE P.PLAYER_ID = '%s'";
    private static final String UPDATE_SESSION_TOKEN_QUERY = "UPDATE PLAYER SET SESSION_TOKEN = '%s', LAST_ACTIVITY = TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS') WHERE PLAYER_ID = '%s'";
    private static final String UPDATE_SESSION_TOKEN_BY_PK_QUERY = "UPDATE PLAYER SET SESSION_TOKEN = '%s', LAST_ACTIVITY = TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS') WHERE PLAYER_KEY = '%d'";
    
    public static String setNewSessionToken(Connection connection, String username, String password) throws SQLException {
        String hashedPassword = getHashedPassword(connection, username);
        if (!verifyPassword(hashedPassword, password)) {
            throw new IllegalArgumentException("Invalid password");
        }
        return generateAndSetNewToken(connection, username);
    }

    public static void removeSessionToken(Connection connection, int playerKey) throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String datetime = now.format(formatter);

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(String.format(UPDATE_SESSION_TOKEN_BY_PK_QUERY, null, datetime, playerKey));
        }
    }
    
    private static String getHashedPassword(Connection connection, String username) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY_GET_HASHEDPW, username));
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }
        
        String hashedPassword = queryResult.getString(1);
        
        statement.close();
        queryResult.close();
        
        return hashedPassword;
    }
    
    private static boolean verifyPassword(String hashedPassword, String password) {
        String[] passwordSalt = hashedPassword.split(";");
        if (passwordSalt.length != 2)
            throw new IllegalStateException("Invalid hashed password detected: " + hashedPassword);
        String passwordPart = passwordSalt[0];
        String salt = passwordSalt[1];
        
        String inputPasswordHashed = PasswordHasher.calculateHashedPassword(password, salt);
        return passwordPart.equals(inputPasswordHashed);
    }
    
    private static String generateAndSetNewToken(Connection connection, String username) throws SQLException {
        Statement statement = connection.createStatement();
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String datetime = now.format(formatter);
        
        // Update session token
        byte[] tokenBytes;
        String newSessionToken;
        while (true) {
            // Create new session token (Base64, 64 chars)
            tokenBytes = new byte[48]; // 48byte --Base64-> 64 chars
            new SecureRandom().nextBytes(tokenBytes);
            newSessionToken = Base64.getEncoder().encodeToString(tokenBytes);
            
            try {
                statement.executeUpdate(String.format(UPDATE_SESSION_TOKEN_QUERY, newSessionToken, datetime, username));
                break;
            } catch (SQLException e) {
                if (e.getErrorCode() != 1) {
                    // Not a 'unique constraint' violation
                    throw e;
                }
            }
        }
        
        statement.close();
        
        return newSessionToken;
    }
}
