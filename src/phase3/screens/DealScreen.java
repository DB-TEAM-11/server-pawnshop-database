package phase3.screens;

import java.util.Scanner;

public class DealScreen extends BaseScreen {
    // private static String  = "";
    private static String TITLE_MAIN = "고객과 거래";
    private static String TITLE_ITEM_INSPECTION = "물건 조사";
    private static String TITLE_GRADE_FIND = "등급 감정";
    private static String TITLE_FLAW_FIND = "흠 찾기";
    private static String TITLE_OPEN_CUSTOMER_HINT = "새 고객 힌트 열기";
    private static String TITLE_ACCEPT_DEAL__CHECK_BALANCE = "구매 전 잔고 확인";
    private static String TITLE_ACCEPT_DEAL__SAVE = "구매 기록 저장";
    private static String TITLE_DENY_DEAL__REMOVE_DEAL = "거래 삭제";

    private static String MESSAGE_DENY_DEAL__REMOVE_DEAL = "거래를 거절하였으므로, 해당 거래 기록을 삭제합니다.";

    private static String[] CHOICES_MAIN = {
        "물건 조사",
        "등급 감정",
        "흠 찾기",
        "진위 판정 [200G 소모]",
        "새 물건 힌트 열기 [10G 소모]",
        "새 고객 힌트 열기",
        "거래 수락",
        "거래 거절",
        "거래 일시정지, 메인으로 돌아가기"
    };
    private static String[] CHOICES_ITEM_INSPECTION = { "물건 힌트 1개 열기 [10G 소모]", "이전 화면으로 돌아가기" };
    private static String[] CHOICES_GRADE_FIND = { "레어 감정 [20G 소모]", "유니크 감정 [30G 소모]", "레전더리 감정 [50G 소모]", "이전 화면으로 돌아가기" };
    private static String[] CHOICES_FLAW_FIND = { "하급 흠 찾기 [20G 소모]", "중급 흠 찾기 [60G 소모]", "고급 흠 찾기 [100G 소모]", "이전 화면으로 돌아가기" };
    private static String[] CHOICES_OPEN_CUSTOMER_HINT = { "'사기칠 거 같은 비율' 힌트 열기 [50G 소모]", "'수집가 능력' 힌트 열기 [50G 소모]", "'대충 관리함' 힌트 열기 [50G 소모]", "이전 화면으로 돌아가기" };
    private static String[] CHOICES_ACCEPT_DEAL__CHECK_BALANCE = { "잔고 확인" };
    private static String[] CHOICES_ACCEPT_DEAL__SAVE = { "저장" };
    private static String[] CHOICES_DENY_DEAL__REMOVE_DEAL = { "삭제" };
    
    public DealScreen(Scanner scanner) {
        super(scanner);
    }

    public void showDealScreen() {
        while (true) {
            switch (showChoices(TITLE_MAIN, formatMainMessage(), CHOICES_MAIN)) {
                case 1:
                    showItemInspectionScreen();
                    break;
                case 2:
                    showGradeFindScreen();
                    break;
                case 3:
                    showFlawFindScreen();
                    break;
                case 4:
                    showItemAuthenticationScreen();
                    break;
                case 5:
                    showItemHintOpenScreen();
                    break;
                case 6:
                    showOpenCustomerHintScreen();
                    break;
                case 7:
                    if (showAcceptDealScreen()) {
                        return;
                    }
                    break;
                case 8:
                    showDenyDealScreen();
                    return;
                case 9:
                    return;
            }
        }
    }

    private String formatMainMessage() {
        // TODO: Implement actual logic

        return "NOT IMPLEMENTED";
    }

    private void showItemInspectionScreen() {
        switch (showChoices(TITLE_ITEM_INSPECTION, CHOICES_ITEM_INSPECTION)) {
            // TODO: Implement actual logic here
            case 1:
                return;
            case 2:
                return;
        }
    }

    private void showGradeFindScreen() {
        switch (showChoices(TITLE_GRADE_FIND, CHOICES_GRADE_FIND)) {
            // TODO: Implement actual logic here
            case 1:
                return;
            case 2:
                return;
            case 3:
                return;
            case 4:
                return;
        }
    }

    private void showFlawFindScreen() {
        switch (showChoices(TITLE_FLAW_FIND, CHOICES_FLAW_FIND)) {
            // TODO: Implement actual logic here
            case 1:
                return;
            case 2:
                return;
            case 3:
                return;
            case 4:
                return;
        }
    }

    private void showItemAuthenticationScreen() {
        // TODO: Implement actual logic
    }

    private void showItemHintOpenScreen() {
        // TODO: Implement actual logic
    }

    private void showOpenCustomerHintScreen() {
        switch (showChoices(TITLE_OPEN_CUSTOMER_HINT, CHOICES_OPEN_CUSTOMER_HINT)) {
            // TODO: Implement actual logic here
            case 1:
                return;
            case 2:
                return;
            case 3:
                return;
            case 4:
                return;
        }
    }

    private boolean showAcceptDealScreen() {
        showChoices(TITLE_ACCEPT_DEAL__CHECK_BALANCE, CHOICES_ACCEPT_DEAL__CHECK_BALANCE, false);
        // TODO: Do actual validation

        showChoices(TITLE_ACCEPT_DEAL__SAVE, CHOICES_ACCEPT_DEAL__SAVE, false);
        // TODO: Do actual save

        return true;
    }

    private void showDenyDealScreen() {
        showChoices(TITLE_DENY_DEAL__REMOVE_DEAL, MESSAGE_DENY_DEAL__REMOVE_DEAL, CHOICES_DENY_DEAL__REMOVE_DEAL, false);
    }
}
