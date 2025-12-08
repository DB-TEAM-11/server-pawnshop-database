package phase4.queries;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import phase4.exceptions.NotASuchRowException;
import phase4.utils.PasswordHasher;

public class SessionTokenSetter {
    private static final String QUERY_GET_HASHEDPW = "SELECT P.HASHED_PW FROM PLAYER P WHERE P.PLAYER_ID = ?";
    private static final String UPDATE_SESSION_TOKEN_QUERY = "UPDATE PLAYER SET SESSION_TOKEN = ?, LAST_ACTIVITY = TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS') WHERE PLAYER_ID = ?";
    private static final String UPDATE_SESSION_TOKEN_BY_PK_QUERY = "UPDATE PLAYER SET SESSION_TOKEN = ?, LAST_ACTIVITY = TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS') WHERE PLAYER_KEY = ?";
    
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

        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SESSION_TOKEN_BY_PK_QUERY)) { 
            statement.setObject(1, null);
            statement.setString(2, datetime);
            statement.setInt(3, playerKey);
            statement.executeUpdate();
        }
    }
    
    private static String getHashedPassword(Connection connection, String username) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY_GET_HASHEDPW)) {
            statement.setString(1, username);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                return queryResult.getString(1);
            }
        }
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
            
            try (PreparedStatement statement = connection.prepareStatement(UPDATE_SESSION_TOKEN_QUERY)) { 
                statement.setString(1, newSessionToken);
                statement.setString(2, datetime);
                statement.setString(3, username);
                statement.executeUpdate();
                break;
            } catch (SQLException e) {
                if (e.getErrorCode() != 1) {
                    // Not a 'unique constraint' violation
                    throw e;
                }
            }
        }
        
        return newSessionToken;
    }
}
