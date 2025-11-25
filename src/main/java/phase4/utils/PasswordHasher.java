package phase4.utils;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordHasher {
    public static String calculateHashedPassword(String password, String salt) {
        MessageDigest digest;
        byte[] hashed;

        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Unsupported: MD5");
        }

        try {
            hashed = digest.digest((password + salt).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("Unsupported: UTF-8");
        }
        
        StringBuilder sb = new StringBuilder();
        for (byte b : hashed) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
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
