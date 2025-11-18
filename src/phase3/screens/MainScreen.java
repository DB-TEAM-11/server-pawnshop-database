package phase3.screens;

import java.sql.Connection;
import java.util.Scanner;

public class MainScreen extends BaseScreen {
    private enum NextScreen {
        DEAL,
        DEBIT_AND_ITEM,
    }
    
    private static final String TITLE_LOAD_PLAYING_GAME_SESSION_REQUEST = "게임 세션 가져오기 요청";
    private static final String TITLE_CREATE_GAME_SESSION_REQUEST = "새 게임 세션 생성 요청";
    private static final String TITLE_DIPLAY_ITEM_REQUEST = "전시 중인 아이템 가져오기 요청";
    private static final String TITLE_CHECK_REMAING_DEAL_REQUEST = "대기 중인 거래 기록 확인 요청";
    private static final String TITLE_CHECK_IS_END_OF_WEEK_REQUEST = "7의 배수 일자인지 확인 요청";
    private static final String TITLE_FINALIZE_DAY = "일일 정산 요청";
    private static final String TITLE_FINALIZE_WEEK = "주간 정산 요청";
    private static final String TITLE_NEXT_DAY_REQUEST = "다음 날로 진행 요청";
    private static final String TITLE_MAIN_MENU = "메인 게임";

    private static final String MESSAGE_LOAD_PLAYING_GAME_SESSION_REQUEST = "진행 중인 게임 세션이 있는지 확인하고, 있다면 불러와야 합니다.";
    private static final String MESSAGE_CREATE_GAME_SESSION_REQUEST = "진행 중인 게임이 없습니다. 새 게임을 시작해야 합니다.";
    private static final String MESSAGE_DIPLAY_ITEM_REQUEST = "게임 진행을 위해서는 전시 중인 아이템을 가져와야 합니다.";
    private static final String MESSAGE_CHECK_REMAING_DEAL_REQUEST = "남은 거래가 있는지 확인해야 합니다. 남은 거래가 있다면, 그 거래를 해야 합니다.";
    private static final String MESSAGE_FINALIZE_DAY = "하루가 모두 끝났습니다. 다음 날로 넘어가기 전에, 일일 정산을 수행해야 합니다.";
    private static final String MESSAGE_FINALIZE_WEEK = "한 주가 모두 끝났습니다. 다음 주로 넘어가기 전에, 주간 정산을 수행해야 합니다.";
    private static final String MESSAGE_NEXT_DAY_REQUEST = "계속 진행하기 위해서는, 다음 날로 넘어가야 합니다.";

    private static final String[] CHOICES_LOAD_PLAYING_GAME_SESSION_REQUEST = { "게임 세션 가져오기" };
    private static final String[] CHOICES_CREATE_GAME_SESSION_REQUEST = { "새 게임 세션 생성" };
    private static final String[] CHOICES_DIPLAY_ITEM_REQUEST = { "전시 중인 아이템 가져오기" };
    private static final String[] CHOICES_CHECK_REMAING_DEAL_REQUEST = { "남은 거래 있는지 확인하기" };
    private static final String[] CHOICES_CHECK_IS_END_OF_WEEK_REQUEST = { "7의 배수 일자인지 확인하기" };
    private static final String[] CHOICES_FINALIZE_DAY = { "일일 정산하기" };
    private static final String[] CHOICES_FINALIZE_WEEK = { "주간 정산하기" };
    private static final String[] CHOICES_NEXT_DAY_REQUEST = { "다음 날로 넘어가기" };
    private static final String[] CHOICES_MAIN_MENU = { "거래 (재개)하기", "빛 상환 / 아이템 처리" };

    private int gameSessionId;
    private DealScreen dealScreen;
    private DebitAndItemScreen debitAndItemScreen;
    
    public MainScreen(Connection connection, Scanner scanner) {
        super(connection, scanner);
        dealScreen = new DealScreen(connection, scanner);
        debitAndItemScreen = new DebitAndItemScreen(connection, scanner);
    }

    public void showMainScreen() {
        showLoadPlayingGameSessionRequest();
        if (gameSessionId == 0) {
            showCreateGameSessionRequest();
        }
        showDiplayItemRequest();

        boolean isFirst = true;
        while (true) {
            int remaingDealCount = showCheckRemaingDealRequest();
            if (remaingDealCount == 0) {
                if (!isFirst) {
                    if (showCheckIsEndOfWeekRequest()) {
                        showFinalizeWeek();
                    } else {
                        showFinalizeDay();
                    }
                }
                showNextDayRequest();
            } else {
                switch (showMainMenu()) {
                    case DEAL:
                        dealScreen.showDealScreen();
                        break;
                    case DEBIT_AND_ITEM:
                        debitAndItemScreen.showDebitAndItemScreen();
                        break;
                }
            }
            isFirst = false;
        }
    }

    private int showLoadPlayingGameSessionRequest() {
        showChoices(TITLE_LOAD_PLAYING_GAME_SESSION_REQUEST, MESSAGE_LOAD_PLAYING_GAME_SESSION_REQUEST, CHOICES_LOAD_PLAYING_GAME_SESSION_REQUEST);

        // TODO: Check playing game session

        return 1;
    }

    private void showCreateGameSessionRequest() {
        showChoices(TITLE_CREATE_GAME_SESSION_REQUEST, MESSAGE_CREATE_GAME_SESSION_REQUEST, CHOICES_CREATE_GAME_SESSION_REQUEST);

        // TODO: Create actual game session
    }

    private void showDiplayItemRequest() {
        showChoices(TITLE_DIPLAY_ITEM_REQUEST, MESSAGE_DIPLAY_ITEM_REQUEST, CHOICES_DIPLAY_ITEM_REQUEST);

        // TODO: Get acutal display item
    }

    private int showCheckRemaingDealRequest() {
        showChoices(TITLE_CHECK_REMAING_DEAL_REQUEST, MESSAGE_CHECK_REMAING_DEAL_REQUEST, CHOICES_CHECK_REMAING_DEAL_REQUEST);

        // TODO: Get actual count of remaining deal(s)

        return 2;
        // return 0;
    }

    private boolean showCheckIsEndOfWeekRequest() {
        showChoices(TITLE_CHECK_IS_END_OF_WEEK_REQUEST, CHOICES_CHECK_IS_END_OF_WEEK_REQUEST, false);

        // TODO: Check it is real end of week

        return false;
    }

    private void showFinalizeDay() {
        showChoices(TITLE_FINALIZE_DAY, MESSAGE_FINALIZE_DAY, CHOICES_FINALIZE_DAY, false);

        // TODO: Calculate & update
    }

    private void showFinalizeWeek() {
        showChoices(TITLE_FINALIZE_WEEK, MESSAGE_FINALIZE_WEEK, CHOICES_FINALIZE_WEEK, false);

        // TODO: Calculate & update
    }

    private void showNextDayRequest() {
        showChoices(TITLE_NEXT_DAY_REQUEST, MESSAGE_NEXT_DAY_REQUEST, CHOICES_NEXT_DAY_REQUEST);

        // TODO: Go to next day
    }

    private NextScreen showMainMenu() {
        switch (showChoices(TITLE_MAIN_MENU, CHOICES_MAIN_MENU)) {
            case 1:
                return NextScreen.DEAL;
            case 2:
                return NextScreen.DEBIT_AND_ITEM;
            default:
                throw new RuntimeException("Invalid choice");
        }
    }
}
