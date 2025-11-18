package phase3.screens;

import java.sql.Connection;
import java.util.Scanner;

public class IntroScreen extends BaseScreen {
    public enum NextScreen {
        MAIN,
        LOGIN
    };
    
    private static final String TITLE = "전당포 운영 게임";
    private static final String[] ACTIONS = { "게임 시작", "월드 레코드", "로그아웃" };

    public IntroScreen(Connection connection, Scanner scanner) {
        super(connection, scanner);
    }

    public NextScreen showIntroScreen() {
        while (true) {
            switch (showChoices(TITLE, ACTIONS)) {
                case 1:
                    return NextScreen.MAIN;
                case 2:
                    showWorldRecord();
                    break;
                case 3:
                    doLogout();
                    return NextScreen.LOGIN;
            }
        }
    }

    private void showWorldRecord() {
        // TODO: Implement querying world record
    }

    private void doLogout() {
        // TODO: Implement logout
    }
}
