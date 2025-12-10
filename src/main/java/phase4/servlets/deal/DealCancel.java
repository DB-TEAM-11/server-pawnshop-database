package phase4.servlets.deal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.constants.ItemState;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.DailyCalculate;
import phase4.queries.DealDeleter;
import phase4.queries.DealRecordByItemState;
import phase4.queries.DisplayedItem;
import phase4.queries.GameSessionUpdater;
import phase4.queries.MoneyUpdater;
import phase4.queries.NewNews;
import phase4.queries.NewsCatalog;
import phase4.queries.NotFoundItemCategory;
import phase4.queries.PlayerInfo;
import phase4.queries.WeeklyCalculate;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;


@WebServlet("/deal/cancel")
public class DealCancel extends JsonServlet {
    private static final long serialVersionUID = 1L;
    
    static private class RequestData {
        int drcKey;
        int itemKey;
    }
    
    static private class ResponseData {
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

        String isGameOvered;
        String isDayNext;
        NextDay dayNext;
        DailyFinalize dayFinalize;
        WorldRecord worldRecord;
        String[] notFoundCategoryList;
    }

    private final SimpleDateFormat gameEndDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    
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
        
        Random random = new Random();
        
        PlayerInfo playerInfo;
        DisplayedItem itemInfo;
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
            
            // Remove deal and item
            DealDeleter.deleteDealRecord(connection, requestData.itemKey);
            DealDeleter.deleteItem(connection, requestData.itemKey);
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
                    if (playerInfo.dayCount % 7 == 0) {
                        // Insert news
                        int newsCount = NewsCatalog.getCount(connection);
                        for (int i = 0; i < random.nextInt(4); i++) {
                            new NewNews(
                                playerInfo.gameSessionKey,
                                random.nextInt(newsCount) + 1,
                                random.nextInt(20) + 1
                            ).insert(connection);
                        }
                    }
                }
            }

            connection.commit();
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        
        ResponseData data = new ResponseData();

        if (notFoundItemCategories != null) {
            data.isGameOvered = "Y";

            data.worldRecord.playerId = playerInfo.playerId;
            data.worldRecord.nickname = playerInfo.nickname;
            data.worldRecord.pawnshopName = playerInfo.shopName;
            data.worldRecord.gameEndDayCount = playerInfo.gameEndDayCount;
            if (playerInfo.gameEndDate != null) {
                data.worldRecord.gameEndDate = gameEndDateFormat.format(playerInfo.gameEndDate);
            }
            
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
