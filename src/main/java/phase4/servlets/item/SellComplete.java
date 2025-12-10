package phase4.servlets.item;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.constants.ItemState;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.CustomerInfo;
import phase4.queries.DealRecordUpdater;
import phase4.queries.ExistingItem;
import phase4.queries.ExistingItemUpdater;
import phase4.queries.MoneyUpdater;
import phase4.queries.PlayerInfo;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;


@WebServlet("/item/sellComplete")
public class SellComplete extends JsonServlet {
    private static final long serialVersionUID = 1L;
    
    private class RequestData {
        int itemKey;
        int customerKey;
    }
    
    private class ResponseData {
        String earnedAmount;
        int leftMoney;
        int displayedPositionKey;
    }
    
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
        if (requestData.itemKey == 0) {
            sendErrorResponse(response, "no_item_key", "No given item key, or item key is 0.");
            return;
        }
        if (requestData.customerKey == 0) {
            sendErrorResponse(response, "no_customer_key", "No given customer key, ir customer key is 0.");
            return;
        }
        
        PlayerInfo playerInfo;
        ExistingItem itemInfo;
        CustomerInfo customerInfo;
        int soldPrice;
        try (Connection connection = SQLConnector.connect()) {
            connection.setAutoCommit(false);

            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_game_session", "No game session exists.");
                return;
            }
            try {
                itemInfo = ExistingItem.getDisplayedItem(connection, requestData.itemKey);
                if (itemInfo.gameSessionKey != playerInfo.gameSessionKey) {
                    sendErrorResponse(response, "not_a_such_item", "Not a such item.");
                }
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "not_a_such_item", "Not a such item.");
                return;
            }
            try {
                customerInfo = CustomerInfo.getCustomerInfo(connection, requestData.customerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_customer", "Not a such customer.");
                return;
            }
            
            if (itemInfo.gameSessionKey != playerInfo.gameSessionKey) {
                sendErrorResponse(response, "not_a_such_item", "Not a such item.");
                return;
            }
            if (itemInfo.categoryKey != customerInfo.categoryKey) {
                sendErrorResponse(response, "category_mismatch", "Item category does not match customer preference.");
                return;
            }
            if (itemInfo.itemState != ItemState.SELLING.value() || itemInfo.itemState != ItemState.RECORVERED_SELLING.value()) {
                sendErrorResponse(response, "not_selling", "Given item is not selling currently.");
                return;
            }
            
            soldPrice = (int)(itemInfo.appraisedPrice * 0.8);
            
            ExistingItem.deleteDisplayedItemEntry(connection, requestData.itemKey);
            ExistingItemUpdater.updateItemState(connection, itemInfo.itemKey, ItemState.SOLD);
            DealRecordUpdater.updateSoldInfo(connection, requestData.itemKey, soldPrice, requestData.customerKey);
            MoneyUpdater.addMoney(connection, playerKey, soldPrice);
            connection.commit();
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        
        int earnedAmount = soldPrice - itemInfo.purchasePrice;

        ResponseData data = new ResponseData();
        data.earnedAmount = (earnedAmount > 0 ? "+" : "") + Integer.toString(earnedAmount);
        data.leftMoney = playerInfo.money + soldPrice;
        data.displayedPositionKey = itemInfo.displayPos;
        
        sendJsonResponse(response, data);
    }
}
