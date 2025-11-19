package phase3.screens;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

import phase3.PlayerSession;
import phase3.constants.ItemState;
import phase3.exceptions.CloseGameException;
import phase3.queries.*;


public class SellScreen extends BaseScreen {
    private static final String TITLE = "물건 구매 요청";
    private static final String DETAIL = "고객이 진열된 %s을/를 구매하고자 합니다.\n구매 희망 가격: %d";
    private static final String[] CHOICES = { "판매", "거절" };

    private PlayerSession session;

    
    public SellScreen(Connection connection, Scanner scanner) {
        super(connection, scanner);
        this.session = PlayerSession.getInstance();
    }

    // 이벤트 수치 반영 가격 배율 계산 함수
    private double getEventMultiplier(int itemCategoryKey) {
        try {
            int playerKey = PlayerKeyByToken.getPlayerKey(connection, session.sessionToken);
            TodaysEvent[] events = TodaysEvent.getTodaysEvent(connection, playerKey);
            
            double multiplier = 1.0;
            for (TodaysEvent event : events) {
                if (event.categoryKey == itemCategoryKey) {
                    double change = event.affectedPrice / 100.0;
                    if (event.plusMinus == 1) {
                        multiplier += change;
                    } else {
                        multiplier -= change;
                    }
                }
            }
            return multiplier;
        } catch (Exception e) {
            // 이벤트가 없거나 오류 발생 시 기본값 1.0 반환
            return 1.0;
        }
    }

    public void showSellScreen(int itemId) {
        DisplayedItemInfo itemInfo = null;
        try {
            itemInfo = DisplayedItemInfo.getDisplayedItemInfo(connection, itemId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        
        // 이벤트 수치 적용
        ItemCatalog itemCatalog = null;
        try {
            itemCatalog = ItemCatalog.getItemByKey(connection, itemInfo.itemCatalogKey);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseGameException();
        }
        double eventMultiplier = getEventMultiplier(itemCatalog.categoryKey);
        
        // 최종 판매가 = 감정가 * (1 - 고객 판매 여부 * 20%) * 이벤트 수치
        int price = (int)(itemInfo.appraisedPrice * 0.8 * eventMultiplier);

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
