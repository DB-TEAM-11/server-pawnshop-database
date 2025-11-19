package phase3.screens;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

import phase3.PlayerSession;
import phase3.constants.ItemState;
import phase3.exceptions.CloseGameException;
import phase3.exceptions.NotASuchRowException;
import phase3.queries.ItemInDisplay;
import phase3.queries.MoneyUpdater;
import phase3.queries.PawnshopDebt;
import phase3.queries.PersonalDebt;
import phase3.queries.PlayerInfo;
import phase3.queries.PlayerKeyByToken;
import phase3.queries.ExistingItemUpdater;

public class DebtAndItemScreen extends BaseScreen {
    private static final String TITLE_MAIN = "빚 대출/상환 & 아이템 경매/복원";
    private static final String TITLE_PERSONAL_DEBT = "개인 빚 상환";
    private static final String TITLE_PERSONAL_DEBT_EXECUTE = "개인 빚 상환 저장";
    private static final String TITLE_PAWNSHOP_DEBT = "가게 빚 상환";
    private static final String TITLE_PAWNSHOP_DEBT_EXECUTE = "가게 빚 상환 저장";
    private static final String TITLE_MOMEY_SAVE = "재산 저장";
    private static final String TITLE_AUCTION_ITEM = "아이템 경매";
    private static final String TITLE_REPAIR_ITEM = "아이템 복원";
    private static final String TITLE_IS_PAWNSHOP_DEBT_EXISTS = "가게 빚 잔액 확인";
    private static final String TITLE_IS_PERSONAL_DEBT_EXISTS = "개인 빚 잔액 확인";

    private static final String MESSAGE_MAIN = "재산: %d | 개인 빚: %d | 가게 빛: %d";
    private static final String MESSAGE_DEBT = "재산: %d | 빚: %d";
    private static final String MESSAGE_IS_PAWNSHOP_DEBT_EXISTS = "개인 빚과 가게 빚이 모두 남아있지 않다면, 게임을 즉시 클리어한 것으로 처리됩니다.";
    private static final String MESSAGE_IS_PERSONAL_DEBT_EXISTS = "가게 빚은 모두 갚으신 것 같습니다. 개인 빚도 확인해야 합니다.";
    private static final String MESSAGE_ITEM_SELECT = "가게 빚은 모두 갚으신 것 같습니다. 개인 빚도 확인해야 합니다.";

    private static final String[] CHOICES_MAIN = { "개인 빚 상환", "가게 빚 대출/상환", "아이템 경매", "아이템 복원", "이전 화면으로 돌아가기" };
    private static final String[] CHOICES_PERSONAL_DEBT = { "상환: 2000", "상환: 1000", "상환: 500", "상환: 100", "취소" };
    private static final String[] CHOICES_PERSONAL_DEBT_EXECUTE = { "개인 빚 상환 실행" };
    private static final String[] CHOICES_PAWNSHOP_DEBT = { "상환: 2000", "상환: 1000", "상환: 500", "상환: 100", "대출: 2000", "대출: 1000", "대출: 500", "대출: 100", "취소" };
    private static final String[] CHOICES_PAWNSHOP_DEBT_EXECUTE = { "가게 빚 대출/상환 실행" };
    private static final String[] CHOICES_MOMEY_SAVE = { "재산 저장" };
    private static final String[] CHOICES_AUCTION_ITEM = { "아이템 상태를 경매 중으로 변경" };
    private static final String[] CHOICES_REPAIR_ITEM = { "아이템 상태를 복원 중으로 변경" };
    private static final String[] CHOICES_IS_PAWNSHOP_DEBT_EXISTS = { "가게 빚 확인" };
    private static final String[] CHOICES_IS_PERSONAL_DEBT_EXISTS = { "개인 빚 확인" };

    private static final String TOO_MUCH_REPAYMENT = "재산보다 상환액이 더 큽니다. 다시 확인해 주세요.";
    private static final String SMALL_DEBT = "빚보다 상환액이 더 큽니다. 다시 확인해 주세요.";
    private static final String INVALID_POSITION = "올바르지 않은 위치입니다. 다시 선택해 주세요.";
    private static final String SHOP_DEBT_REMAIN = "아직 가게 빚이 있습니다.";
    private static final String PERSONAL_DEBT_REMAIN = "아직 개인 빚이 있습니다.";
    private static final String ALL_DEBT_CLEARED = "모든 빚을 갚으셨습니다. 축하합니다!";

    PlayerSession session;

    public DebtAndItemScreen(Connection connection, Scanner scanner) {
        super(connection, scanner);
        this.session = PlayerSession.getInstance();
    }

    public boolean showDebtAndItemScreen() {
        main:
        while (true) {
            PlayerInfo playerInfo;
            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, PlayerKeyByToken.getPlayerKey(connection, session.sessionToken));
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CloseGameException();
            }

            switch (showChoices(TITLE_MAIN, String.format(MESSAGE_MAIN, playerInfo.money, playerInfo.personalDebt, playerInfo.pawnshopDebt), CHOICES_MAIN)) {
                case 1:
                    if (showPersonalDebtScreen(playerInfo)) {
                        return true;
                    }
                    break;
                case 2:
                    if (showShopDebtScreen(playerInfo)) {
                        return true;
                    }
                    break;
                case 3:
                    showItemAuctionScreen();
                    break;
                case 4:
                    showItemRepairScreen();
                    break;
                case 5:
                    break main;
            }
        }
        return false;
    }

    private boolean showPersonalDebtScreen(PlayerInfo playerInfo) {
        int amount;

        while (true) {
            switch (showChoices(TITLE_PERSONAL_DEBT, String.format(MESSAGE_DEBT, playerInfo.money, playerInfo.personalDebt), CHOICES_PERSONAL_DEBT)) {
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
                    return false;
                default:
                    throw new IllegalStateException("Invalid index returned");
            }

            if (playerInfo.money < -amount) {
                System.out.println(TOO_MUCH_REPAYMENT);
                continue;
            }
            if (playerInfo.personalDebt < -amount) {
                System.out.println(SMALL_DEBT);
                continue;
            }

            break;
        }

        showChoices(TITLE_PERSONAL_DEBT_EXECUTE, CHOICES_PERSONAL_DEBT_EXECUTE, false);
        try {
            PersonalDebt.addToPersonalDebt(connection, session.sessionToken, amount);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }

        showChoices(TITLE_MOMEY_SAVE, CHOICES_MOMEY_SAVE, false);
        try {
            MoneyUpdater.addMoney(connection, session.sessionToken, amount);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }

        return showCheckRemainingDebtIsExistRequest();
    }

    private boolean showShopDebtScreen(PlayerInfo playerInfo) {
        int amount;

        while (true) {
            switch (showChoices(TITLE_PAWNSHOP_DEBT, String.format(MESSAGE_DEBT, playerInfo.money, playerInfo.pawnshopDebt), CHOICES_PAWNSHOP_DEBT)) {
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
                    return false;
                default:
                    throw new IllegalStateException("Invalid index returned");
            }

            if (playerInfo.money < -amount) {
                System.out.println(TOO_MUCH_REPAYMENT);
                continue;
            }
            if (playerInfo.pawnshopDebt < -amount) {
                System.out.println(SMALL_DEBT);
                continue;
            }

            break;
        }

        showChoices(TITLE_PAWNSHOP_DEBT_EXECUTE, CHOICES_PAWNSHOP_DEBT_EXECUTE, false);
        try {
            PawnshopDebt.addToShopDebt(connection, session.sessionToken, amount);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }

        showChoices(TITLE_MOMEY_SAVE, CHOICES_MOMEY_SAVE, false);
        try {
            MoneyUpdater.addMoney(connection, session.sessionToken, amount);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }

        if (amount < 0){ // 게임 끝 검사
            return showCheckRemainingDebtIsExistRequest();
        }
        return false; // 게임 끝 검사 안 함
    }

    private void showItemAuctionScreen() {
        ItemInDisplay[] items = null;
        try {
            int playerId = PlayerKeyByToken.getPlayerKey(connection, session.sessionToken);
            try {
                items = ItemInDisplay.getItemInDisplay(connection, playerId);
            } catch (NotASuchRowException e) {
                System.out.println("진열 중인 아이템이 없습니다.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }

        String[] itemNames = {"-", "-", "-", "-", "-", "-", "-", "-", "취소"};
        int[] itemKey = {0, 0, 0, 0, 0, 0, 0, 0};
        for (ItemInDisplay item : items) {
            if (item.itemState == ItemState.DISPLAYING.value() || item.itemState == ItemState.RECORVERED.value()) {
                itemNames[item.displayPos] = String.format("%d번 인덱스: %s", item.displayPos, item.itemCatalogName);
                itemKey[item.displayPos] = item.itemKey;
            }
        }

        while (true) {
            int selection = showChoices(TITLE_AUCTION_ITEM, MESSAGE_ITEM_SELECT, itemNames);
            if (selection == 9 || selection == 0) {
                break; // 취소
            }
            if (selection >= 1 && selection <= 8 && itemKey[selection - 1] > 0) {
                showChoices(TITLE_AUCTION_ITEM, CHOICES_AUCTION_ITEM);
                try {
                    ExistingItemUpdater.updateItemState(connection, itemKey[selection - 1], ItemState.IN_AUCTION.value());
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new CloseGameException();
                }
                break;
            }
            System.out.println(INVALID_POSITION);
        }
    }

    private void showItemRepairScreen() {
        ItemInDisplay[] items = null;
        try {
            int playerId = PlayerKeyByToken.getPlayerKey(connection, session.sessionToken);
            try {
                items = ItemInDisplay.getItemInDisplay(connection, playerId);
            } catch (NotASuchRowException e) {
                System.out.println("진열 중인 아이템이 없습니다.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }

        String[] itemNames = {"-", "-", "-", "-", "-", "-", "-", "-", "취소"};
        int[] itemKey = {0, 0, 0, 0, 0, 0, 0, 0};
        for (ItemInDisplay item : items) {
            if (item.itemState == ItemState.DISPLAYING.value()) {
                itemNames[item.displayPos] = String.format("%d번 인덱스: %s", item.displayPos, item.itemCatalogName);
                itemKey[item.displayPos] = item.itemKey;
            }
        }

        while (true) {
            int selection = showChoices(TITLE_REPAIR_ITEM, MESSAGE_ITEM_SELECT, itemNames);
            if (selection == 9 || selection == 0) {
                break; // 취소
            }
            if (selection >= 1 && selection <= 8 && itemKey[selection - 1] > 0) {
                showChoices(TITLE_REPAIR_ITEM, CHOICES_REPAIR_ITEM);
                try {
                    ExistingItemUpdater.updateItemState(connection, itemKey[selection - 1], ItemState.RECOVERING.value());
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new CloseGameException();
                }
                break;
            }
            System.out.println(INVALID_POSITION);
        }
    }

    private boolean showCheckRemainingDebtIsExistRequest() {
        int shopDebit = Integer.MAX_VALUE, personalDebit = Integer.MAX_VALUE;

        showChoices(TITLE_IS_PAWNSHOP_DEBT_EXISTS, MESSAGE_IS_PAWNSHOP_DEBT_EXISTS, CHOICES_IS_PAWNSHOP_DEBT_EXISTS, false);
        try {
            shopDebit = PawnshopDebt.getShopDebt(connection, session.sessionToken);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }

        if (shopDebit > 0) {
            System.out.println(SHOP_DEBT_REMAIN);
            return false;
        }

        showChoices(TITLE_IS_PERSONAL_DEBT_EXISTS, MESSAGE_IS_PERSONAL_DEBT_EXISTS, CHOICES_IS_PERSONAL_DEBT_EXISTS, false);
        try {
            personalDebit = PersonalDebt.getPersonalDebt(connection, session.sessionToken);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }

        if (personalDebit > 0) {
            System.out.println(PERSONAL_DEBT_REMAIN);
            return false;
        } else {
            System.out.println(ALL_DEBT_CLEARED);
            return true;
        }
    }
}
