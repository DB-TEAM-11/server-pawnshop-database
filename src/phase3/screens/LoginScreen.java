package phase3.screens;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

import phase3.queries.HashedPwGetter;
import phase3.queries.IdIsExist;
import phase3.queries.SessionToken;
import phase3.PlayerSession;
import phase3.exceptions.CloseGameException;
import phase3.queries.AuthenticationCreator;

public class LoginScreen extends BaseScreen {
    private static final String TITLE = "전당포 운영 게임";
    private static final String[] ACTIONS = { "로그인", "회원가입" };
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z]{1,30}");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("[!-~]+");
    
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
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            System.out.println("올바르지 않은 사용자명입니다.");
            return null;
        }
        
        System.out.print("PW: ");
        String password = scanner.nextLine();
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            System.out.println("올바르지 않은 비밀번호입니다.");
            return null;
        }
        
        String hashedPw = HashedPwGetter.GetHashedPw(connection, username);
        if (hashedPw == null) {
            System.out.println("계정이 존재하지 않습니다.");
            return null;
        }
        
        String sessionToken = SessionToken.getSessionTokenByCredentials(connection, username, password, hashedPw);
        PlayerSession.getInstance().setSessionToken(sessionToken);
        
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
        
        while (true) {
            System.out.print("ID (영문 최대 30글자): ");
            username = scanner.nextLine();
            if (username.isBlank()) {
                System.out.println("사용자 이름이 입력되지 않았습니다. 메인 메뉴로 돌아갑니다...");
                return;
            } if (!USERNAME_PATTERN.matcher(username).matches()) {
                System.out.println("올바르지 않은 사용자명입니다.");
            } else {
                boolean idIsExist;
                try {
                    idIsExist = IdIsExist.isIdExist(connection, username);
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new CloseGameException();
                }
                if (idIsExist) {
                    System.out.println("이미 존재하는 사용자명입니다.");
                } else {
                    System.out.println("사용할 수 있는 사용자명입니다.");
                    break;
                }
            }
        }
        
        while (true) {
            System.out.print("PW: ");
            password = scanner.nextLine();
            if (password.isBlank()) {
                System.out.println("비밀번호가 입력되지 않았습니다. 메인 메뉴로 돌아갑니다...");
                return;
            } if (PASSWORD_PATTERN.matcher(password).matches()) {
                break;
            }
            System.out.println("올바르지 않은 비밀번호입니다.");
            continue;
        }
        
        randSalt = SessionToken.getSalt16();
        String hashedPw = HashedPwGetter.sha256(password, randSalt);
        String hashedPwWithSalt = hashedPw + ";" + randSalt;
        
        AuthenticationCreator.createAuthentication(connection, username, hashedPwWithSalt);
        return;
    }
}
