package phase3.screens;

import java.sql.Connection;
import java.util.Scanner;
import phase3.queries.HashedPwGetter;
import phase3.queries.SessionTokenBySign;
import phase3.queries.DuplicateIdChecker;
import phase3.queries.AuthenticationCreator;

public class LoginScreen extends BaseScreen {
    private static final String TITLE = "전당포 운영 게임";
    private static final String[] ACTIONS = { "로그인", "회원가입" };

    public LoginScreen(Connection connection, Scanner scanner) {
        super(connection, scanner);
    }

    public String showLoginScreen() {
        String sessionToken;
        
        while (true) {
            switch (showChoices(TITLE, ACTIONS)) {
                case 1:
                    sessionToken = showLogin();
                    if (sessionToken != null) {
                        return sessionToken;
                    }
                    break;
                case 2:
                	showSignUp();
                    break;
                default:
                    throw new IllegalStateException("Invalid choice");
            }
        }
    }

    private String showLogin() {
        System.out.print("ID: ");
        String username = scanner.nextLine();
        System.out.print("PW: ");
        String password = scanner.nextLine();

        // TODO: Do actual login
        
        String hashedPw = HashedPwGetter.GetHashedPw(connection, username);
        if (hashedPw == null) {
        	System.out.println("계정이 존재하지 않습니다.");
        	return null;
        }
        
        String sessionToken = SessionTokenBySign.SessionTokenBySign(connection, username, password, hashedPw);
        

        if (sessionToken == null) {
        	System.out.println("계정이 존재하지 않습니다.");
          return null;
        } else {
        	System.out.println("로그인에 성공하였습니다.");
        	return sessionToken;
        }
        
    }

    private void showSignUp() {
    		String username = "";
    		String password = "";
    		String randSalt = "";
    		
    		
	    	while(true) {
	        System.out.print("ID(영문 최대 30글자): ");
	        username = scanner.nextLine();
	        if (username.length() > 30) {
	        	System.out.println("30글자 이내로 입력해주세요.");
	        } else if(DuplicateIdChecker.CheckDuplicateId(connection, username)) {
	        	System.out.println("이미 존재하는 사용자명입니다.");
	        } else {
	        	if (AuthenticationCreator.IsEnglishOnly(username)) {
		        	System.out.println("사용할 수 있는 사용자명입니다.");
		        	break;
	        	} else {
		        	System.out.println("영문 최대 30글자만 가능합니다.");
	        	}
	        }
	    	}
        System.out.print("PW: ");
        password = scanner.nextLine();
        randSalt = SessionTokenBySign.getSalt16();
        
        try {
          String hashedPw = HashedPwGetter.sha256(password, randSalt);
          String hashedPwWithSalt = hashedPw + ";" + randSalt;
          
          AuthenticationCreator.CreateAuthentication(connection, username, hashedPwWithSalt);
          
        } catch (Exception e) {
          e.printStackTrace();
        }
        
        

        

        return;
    }
}
