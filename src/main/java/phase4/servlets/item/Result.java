package phase4.servlets.item;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.constants.Grade;
import phase4.constants.ItemState;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.AuctionedItem;
import phase4.queries.DealRecordUpdater;
import phase4.queries.DisplayedItem;
import phase4.queries.ExistingItemUpdater;
import phase4.queries.GameSessionUpdater;
import phase4.queries.MoneyUpdater;
import phase4.queries.NotFoundItemCategory;
import phase4.queries.PlayerInfo;
import phase4.queries.RestoredItem;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;


@WebServlet("/item/result")
public class Result extends JsonServlet {
    private static final long serialVersionUID = 1L;
    
    private class ResponseData {
        class ActionResults {
            ActionResults() {
            }
            ActionResults(int displayedPositionKey, int itemCatalogKey, ItemState itemState, int deltaMoney, int appraisedPrice) {
                this.displayedPositionKey = displayedPositionKey;
                this.itemCatalogKey = itemCatalogKey;
                this.itemState = itemState.value();
                this.deltaMoney = deltaMoney;
                this.appraisedPrice = appraisedPrice;
            }

            int displayedPositionKey;
            int itemCatalogKey;
            int itemState;
            int deltaMoney;
            int appraisedPrice;
        }
        class WorldRecord {
            String playerId;
            String nickname;
            String pawnshopName;
            int gameEndDayCount;
            String gameEndDate;
        }

        ActionResults[] actionResults;
        int leftMoney;
        String isGameOvered;
        String[] notFoundCategoryList;
        WorldRecord worldRecord = new WorldRecord();
    }

    private final SimpleDateFormat gameEndDateFormat = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ");
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int playerKey = authenticateUser(request, response);
        if (playerKey <= 0) {
            return;
        }
        
        ResponseData data = new ResponseData();

        PlayerInfo playerInfo;
        int leftMoney, netChange = 0;
        ArrayList<ResponseData.ActionResults> actionResults = new ArrayList<>();
        String[] notFoundItemCategories = null;
        int appraisedPriceAfterRecover;
        int flawsAfterRecover;
        try (Connection connection = SQLConnector.connect()) {
            connection.setAutoCommit(false);

            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_game_session", "No game session exists.");
                return;
            }
            leftMoney = playerInfo.money;

            AuctionedItem[] auctionedItem = AuctionedItem.getAuctionedItems(connection, playerKey);
            RestoredItem[] restoredItems = RestoredItem.getRestoredItem(connection, playerKey);
            
            for (AuctionedItem item: auctionedItem) {
                netChange += item.appraisedPrice;
                DisplayedItem.deleteDisplayedItemEntry(connection, item.itemKey);
                ExistingItemUpdater.updateItemState(connection, item.itemKey, ItemState.SOLD);
                DealRecordUpdater.updateSoldInfo(connection, item.itemKey, item.appraisedPrice);
                actionResults.add(data.new ActionResults(
                    item.displayPos, item.itemCatalogKey, ItemState.SOLD, item.appraisedPrice, item.appraisedPrice
                ));
            }
            for (RestoredItem item: restoredItems) {
                netChange -= item.foundFlawEa * 10;
                flawsAfterRecover = item.flawEa - item.foundFlawEa;
                
                appraisedPriceAfterRecover = item.appraisedPrice + (int)(
                    item.askingPrice
                    * (flawsAfterRecover * 0.05)
                    * (Grade.priceMultiplier[item.grade])
                    // * ()  // TODO: Add news parameters
                );
                if (!item.authenticity) {
                    if (item.isAuthenticityFound) {
                        appraisedPriceAfterRecover = (int)(appraisedPriceAfterRecover * 0.8 * 0.7);
                    } else {
                        appraisedPriceAfterRecover = (int)(appraisedPriceAfterRecover * 0.8);
                    }
                }
                DealRecordUpdater.updateAppraisedPrice(connection, item.itemKey, appraisedPriceAfterRecover);
                
                if (!item.isAuthenticityFound) {
                    ExistingItemUpdater.updateAuthenticityFound(connection, item.itemKey);
                }
                ExistingItemUpdater.setFlaws(connection, item.itemKey, flawsAfterRecover);
                ExistingItemUpdater.updateItemState(connection, item.itemKey, ItemState.RECORVERED);
                
                actionResults.add(data.new ActionResults(
                    item.displayPos, item.itemCatalogKey, ItemState.RECORVERED, -item.foundFlawEa * 10, item.appraisedPrice
                ));
            }
            
            if (netChange != 0) {
                MoneyUpdater.addMoney(connection, playerKey, netChange);
                leftMoney += netChange;
            }
            if (leftMoney < 0) {
                GameSessionUpdater.setGameEnd(connection, playerInfo.gameSessionKey, false);
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
                notFoundItemCategories = NotFoundItemCategory.getNotFoundItemCategories(connection, playerKey);
            }

            connection.commit();
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        
        data.actionResults = actionResults.toArray(new ResponseData.ActionResults[0]);
        data.leftMoney = leftMoney;
        data.isGameOvered = leftMoney < 0 ? "Y" : "N";
        data.notFoundCategoryList = notFoundItemCategories;
        data.worldRecord.playerId = playerInfo.playerId;
        data.worldRecord.nickname = playerInfo.nickname;
        data.worldRecord.pawnshopName = playerInfo.shopName;
        data.worldRecord.gameEndDayCount = playerInfo.gameEndDayCount;
        data.worldRecord.gameEndDate = gameEndDateFormat.format(playerInfo.gameEndDate);
        
        sendJsonResponse(response, data);
    }
}
