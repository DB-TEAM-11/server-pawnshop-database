package phase4.servlets.deal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.constants.AffectedPrice;
import phase4.constants.Grade;
import phase4.constants.ItemState;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.DailyCalculate;
import phase4.queries.DealRecordByItemState;
import phase4.queries.DealRecordUpdater;
import phase4.queries.DisplayedItem;
import phase4.queries.ExistingItemUpdater;
import phase4.queries.GameSessionUpdater;
import phase4.queries.MoneyUpdater;
import phase4.queries.NotFoundItemCategory;
import phase4.queries.PlayerInfo;
import phase4.queries.TodaysEvent;
import phase4.queries.WeeklyCalculate;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;


@WebServlet("/deal/complete")
public class DealComplete extends JsonServlet {
    private static final long serialVersionUID = 1L;
    
    static private class RequestData {
        int drcKey;
        int itemKey;
    }
    
    static private class ResponseData {
        static class DisplayedItem {
            int displayPositionKey;
            int askingPrice;
            int purchasePrice;
            int appraisedPrice;
            int boughtDate;
            String sellerName;
            int foundGrade;
            int foundFlawEa;
            int foundAuthenticity;
            int itemState;
            int itemKey;
            int itemCatalogKey;
        }

        static class NextDay {
            int dayCount;
            int leftMoney;
            int personalDebt;
            int pawnshopDebt;
        }

        static class DailyFinalize {
            int startMoney;
            int todayEndMoney;
            int interest;
            int weeklyInterest;
            int finalMoney;
        }

        static class WorldRecord {
            String playerId;
            String nickname;
            String pawnshopName;
            int gameEndDayCount;
            String gameEndDate;
        }

        int leftMoney;
        DisplayedItem displayedItem;
        String isGameOvered;
        String isDayNext;
        NextDay dayNext;
        DailyFinalize dayFinalize;
        WorldRecord worldRecord;
        String[] notFoundCategoryList;
    }

    private final SimpleDateFormat gameEndDateFormat = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ");
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int playerKey = authenticateUser(request, response);
        if (playerKey <= 0) {
            return;
        }
        
        RequestData requestData;
        try {
            requestData = gson.fromJson(request.getReader().lines().collect(Collectors.joining("\n")), RequestData.class);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, "invalid_data", "Received malformed data");
            return;
        }
        if (requestData.drcKey == 0) {
            sendErrorResponse(response, "no_deal_record_key", "No deal record provided.");
            return;
        }
        if (requestData.itemKey == 0) {
            sendErrorResponse(response, "no_item_key", "No item provided.");
            return;
        }
        
        PlayerInfo playerInfo;
        DisplayedItem itemInfo;
        int eventPricePercent = 100;
        int purchasePrice;
        ResponseData.DailyFinalize dailyFinalize = null;
        String[] notFoundItemCategories = null;
        try (Connection connection = SQLConnector.connect()) {
            connection.setAutoCommit(false);

            // Verifying request
            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_game_session", "No game session exists.");
                return;
            }
            try {
                itemInfo = DisplayedItem.getDisplayedItem(connection, playerKey, requestData.itemKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_game_session", "No game session exists.");
                return;
            }
            if (itemInfo.dealRecordKey != requestData.drcKey) {
                sendErrorResponse(response, "deal_record_key_mismatch", "Deal record isn't match with item.");
                return;
            }

            // Process purchase
            for (TodaysEvent event: TodaysEvent.getTodaysEvent(connection, playerInfo.gameSessionKey)) {
                if (event.affectedPrice == AffectedPrice.PURCHASE.value()) {
                    eventPricePercent += event.amount;
                }
            }
            purchasePrice = (int)(
                itemInfo.askingPrice
                * (1 - itemInfo.foundFlawEa * 0.05)
                * Grade.priceMultiplier[itemInfo.grade]
                * (eventPricePercent * 0.01)
            );
            if (!itemInfo.authenticity && itemInfo.isAuthenticityFound) {
                purchasePrice /= 2;
            }

            if (purchasePrice > playerInfo.money) {
                sendErrorResponse(response, "not_enough_money", "Money is not enough to purchase.");
                return;
            }

            ExistingItemUpdater.updateItemState(connection, requestData.itemKey, ItemState.DISPLAYING);
            itemInfo.itemState = ItemState.DISPLAYING.value();
            DealRecordUpdater.updatePurchaseInfo(connection, requestData.itemKey, purchasePrice);
            itemInfo.boughtDate = playerInfo.dayCount;
            itemInfo.purchasePrice = purchasePrice;
            MoneyUpdater.addMoney(connection, playerKey, -purchasePrice);
            playerInfo.money -= purchasePrice;
            
            connection.commit();

            // Check if remaining deal is exists
            boolean isDealReamining;
            try {
                DealRecordByItemState.getDealRecordByItemState(connection, playerKey, ItemState.CREATED);
                isDealReamining = true;
            } catch (NotASuchRowException e) {
                isDealReamining = false;
            }

            if (!isDealReamining) {
                // Do finalize
                dailyFinalize = new ResponseData.DailyFinalize();
                if (playerInfo.dayCount % 7 == 0) {
                    // Weekly finalize
                    WeeklyCalculate result = WeeklyCalculate.getWeeklyCaluclate(connection, playerKey);
                    dailyFinalize.startMoney = result.todayStart;
                    dailyFinalize.todayEndMoney = result.todayEnd;
                    dailyFinalize.interest = result.todayInterest;
                    dailyFinalize.weeklyInterest = result.todayPersonalInterest;
                    dailyFinalize.finalMoney = result.todayFinal;
                } else {
                    // Daily finalize
                    DailyCalculate result = DailyCalculate.getDailyCalculate(connection, playerKey);
                    dailyFinalize.startMoney = result.todayStart;
                    dailyFinalize.todayEndMoney = result.todayEnd;
                    dailyFinalize.interest = result.todayInterest;
                    dailyFinalize.weeklyInterest = 0;
                    dailyFinalize.finalMoney = result.todayFinal;
                }

                if (dailyFinalize.finalMoney - playerInfo.money != 0) {
                    MoneyUpdater.addMoney(connection, playerKey, dailyFinalize.finalMoney - playerInfo.money);
                    playerInfo.money = dailyFinalize.finalMoney;
                }
                if (dailyFinalize.finalMoney < 0) {
                    // Defeat
                    GameSessionUpdater.setGameEnd(connection, playerInfo.gameSessionKey, false);
                    playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
                    notFoundItemCategories = NotFoundItemCategory.getNotFoundItemCategories(connection, playerKey);
                } else {
                    // Move to next day
                    GameSessionUpdater.incrementDayCount(connection, playerInfo.gameSessionKey);
                    playerInfo.dayCount++;
                }
            }

            connection.commit();
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        
        ResponseData data = new ResponseData();
        data.leftMoney = playerInfo.money;

        data.displayedItem.displayPositionKey = itemInfo.displayPos;
        data.displayedItem.askingPrice = itemInfo.askingPrice;
        data.displayedItem.purchasePrice = itemInfo.purchasePrice;
        data.displayedItem.appraisedPrice = itemInfo.appraisedPrice;
        data.displayedItem.boughtDate = itemInfo.boughtDate;
        data.displayedItem.sellerName = itemInfo.customerName;
        data.displayedItem.foundGrade = itemInfo.foundGrade;
        data.displayedItem.foundFlawEa = itemInfo.foundFlawEa;
        data.displayedItem.foundAuthenticity = itemInfo.isAuthenticityFound ? (itemInfo.authenticity ? 1 : 0) : -1;
        data.displayedItem.itemState = ItemState.DISPLAYING.value();
        data.displayedItem.itemKey = itemInfo.itemKey;
        data.displayedItem.itemCatalogKey = itemInfo.itemCatalogKey;

        if (notFoundItemCategories != null) {
            data.isGameOvered = "Y";

            data.worldRecord.playerId = playerInfo.playerId;
            data.worldRecord.nickname = playerInfo.nickname;
            data.worldRecord.pawnshopName = playerInfo.shopName;
            data.worldRecord.gameEndDayCount = playerInfo.gameEndDayCount;
            data.worldRecord.gameEndDate = gameEndDateFormat.format(playerInfo.gameEndDate);

            data.notFoundCategoryList = notFoundItemCategories;
        } else {
            data.isGameOvered = "N";
            data.worldRecord = null;
            data.notFoundCategoryList = null;
        }

        if (dailyFinalize != null) {
            data.isDayNext = "Y";

            data.dayNext.dayCount = playerInfo.dayCount;
            data.dayNext.leftMoney = playerInfo.money;
            data.dayNext.personalDebt = playerInfo.personalDebt;
            data.dayNext.pawnshopDebt = playerInfo.pawnshopDebt;

            data.dayFinalize = dailyFinalize;
        } else {
            data.isDayNext = "N";
            data.dayNext = null;
            data.dayFinalize = null;
        }

        sendJsonResponse(response, data);
    }
}
