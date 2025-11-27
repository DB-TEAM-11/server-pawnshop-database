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
import phase4.queries.DisplayedItem;
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
        int earnedAmount;
        int leftMoney;
        int displayedPositionKey;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        DisplayedItem itemInfo;
        CustomerInfo customerInfo;
        int soldPrice;
        try (Connection connection = SQLConnector.connect()) {
            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_game_session", "No game session exists.");
                return;
            }
            try {
                itemInfo = DisplayedItem.getDisplayedItem(connection, requestData.itemKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_item", "Not a such item.");
                return;
            }
            try {
                customerInfo = CustomerInfo.getCustomerInfo(connection, requestData.customerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_customer", "Not a such customer.");
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
            
            if (playerInfo.money < itemInfo.money) {
                sendErrorResponse(response, "not_enough_money", "not enough money to complete the deal.");
            }
            
            soldPrice = (int)(itemInfo.appraisedPrice * 0.8);
            
            DealRecordUpdater.updateAsSold(connection, requestData.itemKey, soldPrice, requestData.customerKey);
            DisplayedItem.deleteDisplayedItemEntry(connection, requestData.itemKey);
            MoneyUpdater.addMoney(connection, playerKey, -soldPrice);
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        
        ResponseData data = new ResponseData();
        data.earnedAmount = soldPrice - itemInfo.purchasePrice;
        data.leftMoney = playerInfo.money - soldPrice;
        data.displayedPositionKey = itemInfo.displayPos;
        
        sendJsonResponse(response, data);
    }
}
