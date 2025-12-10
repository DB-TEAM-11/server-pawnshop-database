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
import phase4.queries.ExistingItem;
import phase4.queries.ExistingItemUpdater;
import phase4.queries.PlayerInfo;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;


@WebServlet("/item/sellStart")
public class SellStart extends JsonServlet {
    private static final long serialVersionUID = 1L;
    
    private class RequestData {
        int customerKey;
    }
    
    private class ResponseData {
        int itemKey;
        int sellingPrice;
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
        if (requestData.customerKey == 0) {
            sendErrorResponse(response, "no_customer_key", "No given customer key, ir customer key is 0.");
            return;
        }
        
        PlayerInfo playerInfo;
        ExistingItem itemInfo;
        CustomerInfo customerInfo;
        try (Connection connection = SQLConnector.connect()) {
            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_game_session", "No game session exists.");
                return;
            }
            try {
                customerInfo = CustomerInfo.getCustomerInfo(connection, requestData.customerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_customer", "Not a such customer.");
                return;
            }

            itemInfo = ExistingItem.getPreferableItem(connection, requestData.customerKey);
            if (itemInfo == null) {
                sendErrorResponse(response, "no_matched_item", "Item category does not match customer preference.");
                return;
            }
            if (itemInfo.categoryKey != customerInfo.categoryKey) {
                sendErrorResponse(response, "category_mismatch", "Item category does not match customer preference.");
                return;
            }
            
            if (itemInfo.itemState == ItemState.DISPLAYING.value()) {
                ExistingItemUpdater.updateItemState(connection, itemInfo.itemKey, ItemState.SELLING);
            } else if (itemInfo.itemState == ItemState.RECORVERED.value()) {
                ExistingItemUpdater.updateItemState(connection, itemInfo.itemKey, ItemState.RECORVERED_SELLING);
            } else {
                sendErrorResponse(response, 500, "not_selling", "The selected item is not selling currently.");
                return;
            }
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        
        ResponseData data = new ResponseData();
        data.itemKey = itemInfo.itemKey;
        data.sellingPrice = (int)(itemInfo.appraisedPrice * 0.8);
        
        sendJsonResponse(response, data);
    }
}
