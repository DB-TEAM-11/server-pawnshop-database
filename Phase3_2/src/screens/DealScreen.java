package screens;

import java.sql.Connection;
import java.util.Scanner;

import Main.PlayerSession;

public class DealScreen extends BaseScreen {
    // private static String  = "";
    private static String TITLE_MAIN = "怨좉컼怨� 嫄곕옒";
    private static String TITLE_ITEM_INSPECTION = "臾쇨굔 議곗궗";
    private static String TITLE_GRADE_FIND = "�벑湲� 媛먯젙";
    private static String TITLE_FLAW_FIND = "�씈 李얘린";
    private static String TITLE_OPEN_CUSTOMER_HINT = "�깉 怨좉컼 �엺�듃 �뿴湲�";
    private static String TITLE_ACCEPT_DEAL__CHECK_BALANCE = "援щℓ �쟾 �옍怨� �솗�씤";
    private static String TITLE_ACCEPT_DEAL__SAVE = "援щℓ 湲곕줉 ���옣";
    private static String TITLE_DENY_DEAL__REMOVE_DEAL = "嫄곕옒 �궘�젣";

    private static String MESSAGE_DENY_DEAL__REMOVE_DEAL = "嫄곕옒瑜� 嫄곗젅�븯���쑝誘�濡�, �빐�떦 嫄곕옒 湲곕줉�쓣 �궘�젣�빀�땲�떎.";

    private static String[] CHOICES_MAIN = {
        "臾쇨굔 議곗궗",
        "�벑湲� 媛먯젙",
        "�씈 李얘린",
        "吏꾩쐞 �뙋�젙 [200G �냼紐�]",
        "�깉 臾쇨굔 �엺�듃 �뿴湲� [10G �냼紐�]",
        "�깉 怨좉컼 �엺�듃 �뿴湲�",
        "嫄곕옒 �닔�씫",
        "嫄곕옒 嫄곗젅",
        "嫄곕옒 �씪�떆�젙吏�, 硫붿씤�쑝濡� �룎�븘媛�湲�"
    };
    private static String[] CHOICES_ITEM_INSPECTION = { "臾쇨굔 �엺�듃 1媛� �뿴湲� [10G �냼紐�]", "�씠�쟾 �솕硫댁쑝濡� �룎�븘媛�湲�" };
    private static String[] CHOICES_GRADE_FIND = { "�젅�뼱 媛먯젙 [20G �냼紐�]", "�쑀�땲�겕 媛먯젙 [30G �냼紐�]", "�젅�쟾�뜑由� 媛먯젙 [50G �냼紐�]", "�씠�쟾 �솕硫댁쑝濡� �룎�븘媛�湲�" };
    private static String[] CHOICES_FLAW_FIND = { "�븯湲� �씈 李얘린 [20G �냼紐�]", "以묎툒 �씈 李얘린 [60G �냼紐�]", "怨좉툒 �씈 李얘린 [100G �냼紐�]", "�씠�쟾 �솕硫댁쑝濡� �룎�븘媛�湲�" };
    private static String[] CHOICES_OPEN_CUSTOMER_HINT = { "'�궗湲곗튌 嫄� 媛숈� 鍮꾩쑉' �엺�듃 �뿴湲� [50G �냼紐�]", "'�닔吏묎� �뒫�젰' �엺�듃 �뿴湲� [50G �냼紐�]", "'��異� 愿�由ы븿' �엺�듃 �뿴湲� [50G �냼紐�]", "�씠�쟾 �솕硫댁쑝濡� �룎�븘媛�湲�" };
    private static String[] CHOICES_ACCEPT_DEAL__CHECK_BALANCE = { "�옍怨� �솗�씤" };
    private static String[] CHOICES_ACCEPT_DEAL__SAVE = { "���옣" };
    private static String[] CHOICES_DENY_DEAL__REMOVE_DEAL = { "�궘�젣" };
    
    // 寃뚯엫 �꽭�뀡 �젙蹂� ���옣 �떛湲��넠(�쟾�뿭 蹂��닔 �뒓�굦�쑝濡� �궗�슜)
    private PlayerSession session;

    public DealScreen(Connection connection, Scanner scanner) {
        super(connection, scanner);
        this.session = PlayerSession.getInstance();
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
