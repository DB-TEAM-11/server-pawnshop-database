package phase3.screens;

import java.util.Scanner;

public class DebitAndItemScreen extends BaseScreen {
    private static final String TITLE_MAIN = "빚 대출/상환 & 아이템 경매/복원";
    private static final String TITLE_PERSONAL_DEBIT = "개인 빚 상환";
    private static final String TITLE_SHOP_DEBIT = "가게 빚 상환";
    private static final String TITLE_CHECK_DEBIT = "상환 금액이 유효한지 확인하기";
    private static final String TITLE_CHECK_REMAINING_DEBIT_IS_EXIST_REQUEST = "빚 잔액 확인";

    private static final String MESSAGE_MAIN = "재산: %d\n개인 빚: %d\n가게 빛: %d";
    private static final String MESSAGE_CHECK_REMAINING_DEBIT_IS_EXIST_REQUEST = "더 이상 빚이 남아있지 않다면, 게임을 즉시 클리어한 것으로 처리됩니다.";

    private static final String[] CHOICES_MAIN = { "개인 빚 상환", "가게 빚 대출/상환", "아이템 경매/복원", "이전 화면으로 돌아가기" };
    private static final String[] CHOICES_PERSONAL_DEBIT = { "상환: 2000", "상환: 1000", "상환: 500", "상환: 100", "취소" };
    private static final String[] CHOICES_SHOP_DEBIT = { "상환: 2000", "상환: 1000", "상환: 500", "상환: 100", "대출: 2000", "대출: 1000", "대출: 500", "대출: 100", "취소" };
    private static final String[] CHOICES_CHECK_DEBIT = { "상환 금액 유효성 검증" };
    private static final String[] CHOICES_CHECK_REMAINING_DEBIT_IS_EXIST_REQUEST = { "남은 빚 있는지 확인하기" };

    public DebitAndItemScreen(Scanner scanner) {
        super(scanner);
    }

    public boolean showDebitAndItemScreen() {
        main:
        while (true) {
            switch (showChoices(TITLE_MAIN, formatMenuMessage(), CHOICES_MAIN)) {
                case 1:
                    showPersonalDebitScreen();
                    if (showCheckRemainingDebitIsExistRequest()) {
                        return true;
                    }
                    break;
                case 2:
                    showShopDebitScreen();
                    if (showCheckRemainingDebitIsExistRequest()) {
                        return true;
                    }
                    break;
                case 3:
                    break;
                case 4:
                    break main;
            }
        }
        return false;
    }

    private String formatMenuMessage() {
        // TODO: fill message with actual value
        // String message = MESSAGE_MAIN.format()
        String message = MESSAGE_MAIN;
        return message;
    }

    private void showPersonalDebitScreen() {
        int amount;

        while (true) {
            switch (showChoices(TITLE_PERSONAL_DEBIT, CHOICES_PERSONAL_DEBIT)) {
                case 1:
                    amount = -2000;
                    break;
                case 2:
                    amount = -1000;
                    break;
                case 3:
                    amount = -500;
                    break;
                case 4:
                    amount = -100;
                    break;
                case 5:
                    return;
                default:
                    throw new RuntimeException("Invalid index");
            }

            showChoices(TITLE_CHECK_DEBIT, CHOICES_CHECK_DEBIT);
            // TODO: Perform amount check
            // if () {
            break;
            // }
        }

        // TODO: Implement actual action
    }

    private void showShopDebitScreen() {
        int amount;

        while (true) {
            switch (showChoices(TITLE_SHOP_DEBIT, CHOICES_SHOP_DEBIT)) {
                case 1:
                    amount = -2000;
                    break;
                case 2:
                    amount = -1000;
                    break;
                case 3:
                    amount = -500;
                    break;
                case 4:
                    amount = -100;
                    break;
                case 5:
                    amount = 2000;
                    break;
                case 6:
                    amount = 1000;
                    break;
                case 7:
                    amount = 500;
                    break;
                case 8:
                    amount = 100;
                    break;
                case 9:
                    return;
                default:
                    throw new RuntimeException("Invalid index");
            }

            if (amount < 0) {
                showChoices(TITLE_CHECK_DEBIT, CHOICES_CHECK_DEBIT);
            }
            // TODO: Perform amount check
            // if () {
            break;
            // }
        }

        // TODO: Implement actual action
    }

    private boolean showCheckRemainingDebitIsExistRequest() {
        showChoices(TITLE_CHECK_REMAINING_DEBIT_IS_EXIST_REQUEST, MESSAGE_CHECK_REMAINING_DEBIT_IS_EXIST_REQUEST, CHOICES_CHECK_REMAINING_DEBIT_IS_EXIST_REQUEST, false);

        // TODO: implement check

        return false;
    }
}
