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
import phase4.queries.DisplayedItem;
import phase4.queries.ExistingItemUpdater;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;


@WebServlet("/item/sellCancel")
public class SellCancel extends JsonServlet {
    private static final long serialVersionUID = 1L;
    
    private class RequestData {
        int itemKey;
        int customerKey;
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
        
        DisplayedItem itemInfo;
        CustomerInfo customerInfo;
        try (Connection connection = SQLConnector.connect()) {
            try {
                itemInfo = DisplayedItem.getDisplayedItem(connection, playerKey, requestData.itemKey);
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
            
            if (itemInfo.itemState == ItemState.SELLING.value()) {
                ExistingItemUpdater.updateItemState(connection, requestData.itemKey, ItemState.DISPLAYING);
            } else if (itemInfo.itemState == ItemState.RECORVERED_SELLING.value()) {
                ExistingItemUpdater.updateItemState(connection, requestData.itemKey, ItemState.RECORVERED);
            } else {
                sendErrorResponse(response, "not_selling", "Given item is not selling currently.");
                return;
            }
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        
        sendEmptyJsonResponse(response);
    }
}
