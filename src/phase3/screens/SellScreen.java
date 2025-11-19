package phase3.screens;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

import phase3.PlayerSession;
import phase3.constants.ItemState;
import phase3.exceptions.CloseGameException;
import phase3.queries.DeleteDeal;
import phase3.queries.DisplayedItemInfo;
import phase3.queries.UpdateExistingItem;
import phase3.queries.UpdateMoney;

public class SellScreen extends BaseScreen {
    private static final String TITLE = "물건 구매 요청";
    private static final String DETAIL = "고객이 진열된 %s을/를 구매하고자 합니다.\n구매 희망 가격: %d";
    private static final String[] CHOICES = { "판매", "거절" };

    private PlayerSession session;

    
    public SellScreen(Connection connection, Scanner scanner) {
        super(connection, scanner);
        this.session = PlayerSession.getInstance();
    }

    public void showSellScreen(int itemId) {
        DisplayedItemInfo itemInfo = null;
        try {
            itemInfo = DisplayedItemInfo.getDisplayedItemInfo(connection, itemId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        int price = (int)(itemInfo.appraisedPrice * 0.8);

        switch (showChoices(TITLE, String.format(DETAIL, itemInfo.itemCatalogName, price), CHOICES)) {
            case 1:
                try {
                    UpdateExistingItem.updateItemState(connection, itemId, ItemState.SOLD.value());
                    UpdateMoney.addMoney(connection, session.sessionToken, price);
                    connection.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new CloseGameException();
                }
                break;
            case 2:
                try {
                    DeleteDeal.deleteDealRecord(connection, itemInfo.dealRecordKey);
                    DeleteDeal.deleteItem(connection, itemInfo.itemKey);
                    connection.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new CloseGameException();
                }
                break;
        }
    }
}
