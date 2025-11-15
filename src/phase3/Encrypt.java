package phase3;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Encrypt {
	public String hashedpw_with_salt(String pwd) {
		
		String salt = this.getSalt();
		
		String hashed_pw = this.getEncrypt(pwd, salt);
		
		String res = hashed_pw + ";" + salt;
		
		return res;
	}

	private String getSalt() {
		SecureRandom r = new SecureRandom();
		byte[] salt = new byte[8];
		
		r.nextBytes(salt);
		
		StringBuffer sb = new StringBuffer();
		for (byte b : salt) {
			sb.append(String.format("%02x", b));
		}
		
		return sb.toString();
	}
	
	public String getEncrypt(String pwd, String salt) {
		String result = "";
		
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			
			md.update((pwd + salt).getBytes());
			byte[] pwdsalt = md.digest();
			
			StringBuffer sb = new StringBuffer();
			for (byte b: pwdsalt) {
				sb.append(String.format("%02x", b));
			}
			
			result = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return result;
	}

}
