package phase3.screens;

import java.sql.Connection;
import java.util.Scanner;

public class SellScreen extends BaseScreen {
    private static final String TITLE = "물건 구매 요청";
    private static final String DETAIL = "고객이 진열된 %s을/를 구매하고자 합니다.\n구매 희망 가격: %d";
    private static final String[] CHOICES = { "판매", "거절" };

    
    public SellScreen(Connection connection, Scanner scanner) {
        super(connection, scanner);
    }

    public void showSellScreen(int itemId) {
        switch (showChoices(TITLE, getDetail(itemId), CHOICES)) {
            case 1:
                // TODO: Mark the item as sold
                break;
            case 2:
                break;
        }
    }

    private String getDetail(int itemId) {
        // TODO: implement here
        return DETAIL;
    }
}
