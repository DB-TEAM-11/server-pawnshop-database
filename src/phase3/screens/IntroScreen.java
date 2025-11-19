package phase3.screens;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import phase3.PlayerSession;
import phase3.exceptions.CloseGameException;
import phase3.queries.PlayerUpdater;
import phase3.queries.WorldRecord;

public class IntroScreen extends BaseScreen {
    public enum NextScreen {
        MAIN,
        LOGIN
    };
    
    private static final String TITLE = "전당포 운영 게임";
    private static final String[] ACTIONS = { "게임 시작", "월드 레코드", "로그아웃" };

    private PlayerSession session;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public IntroScreen(Connection connection, Scanner scanner) {
        super(connection, scanner);
        this.session = PlayerSession.getInstance();
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
        WorldRecord[] worldRecords = null;

        try {
            worldRecords = WorldRecord.retrieveWorldRecord(connection);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }

        System.out.println("플레이어명 | 닉네임 | 가게 이름 | 게임 진행한 날짜 | 게임 끝난 날");
        for (WorldRecord worldRecord : worldRecords) {
            System.out.printf(
                "%6s %6s %6s %13d %6s\n",
                worldRecord.playerId,
                worldRecord.nickName,
                worldRecord.shopName,
                worldRecord.gameEndDayCount,
                dateFormat.format(worldRecord.gameEndDate)
            );
        }
    }

    private void doLogout() {
        try {
            PlayerUpdater.logout(connection, session.sessionToken);
            PlayerSession.getInstance().reset();
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
    }
}
