package phase3.screens;

import java.sql.Connection;
import java.util.Scanner;

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
                    showSignIn();
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

        return "";
    }

    private void showSignIn() {
        System.out.print("ID: ");
        String username = scanner.nextLine();
        System.out.print("PW: ");
        String password = scanner.nextLine();

        // TODO: Do actual signin
        // if (checkDuplicate(username)) {
        //     System.out.println("이미 존재하는 사용자명입니다.");
        // }

        return;
    }
}
