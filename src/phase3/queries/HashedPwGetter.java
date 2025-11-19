package phase3.queries;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Statement;

import phase3.exceptions.CloseGameException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HashedPwGetter {
    private static final String QUERY = "SELECT P.HASHED_PW FROM PLAYER P WHERE P.PLAYER_ID = '%s'";
    
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
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        return hashed_pw;
    }
    
    public static String sha256(String pw, String salt) {
        MessageDigest md;
        byte[] hashed;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        try {
            hashed = md.digest((pw + salt).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        StringBuilder sb = new StringBuilder();
        for (byte b : hashed) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
