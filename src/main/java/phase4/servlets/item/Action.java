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
import phase4.queries.DisplayedItem;
import phase4.queries.ExistingItemUpdater;
import phase4.queries.PlayerInfo;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;


@WebServlet("/item/action")
public class Action extends JsonServlet {
    private static final long serialVersionUID = 1L;
    
    private class RequestData {
        String actionType;
        int itemKey;
    }
    
    private class ResponseData {
        int itemState;
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
        if (requestData.actionType == null) {
            sendErrorResponse(response, "no_action_type", "No action specified.");
            return;
        }
        if (requestData.itemKey == 0) {
            sendErrorResponse(response, "no_item_key", "No item specified.");
            return;
        }

        ItemState newItemState;
        switch (requestData.actionType) {
            case "restore":
                newItemState = ItemState.RECOVERING;
                break;
            case "auction":
                newItemState = ItemState.IN_AUCTION;
                break;
            default:
                sendErrorResponse(response, "invalid_action_type", "Invalid action specified.");
                return;
        }
        
        PlayerInfo playerInfo;
        DisplayedItem itemInfo;
        try (Connection connection = SQLConnector.connect()) {
            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_game_session", "No game session exists.");
                return;
            }
            try {
                itemInfo = DisplayedItem.getDisplayedItem(connection, playerKey, requestData.itemKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_item", "Not a such item.");
                return;
            }

            if (itemInfo.gameSessionKey != playerInfo.gameSessionKey) {
                sendErrorResponse(response, "no_item", "Not a such item.");
                return;
            }
            if (itemInfo.itemState != ItemState.DISPLAYING.value() || itemInfo.itemState != ItemState.RECORVERED.value()) {
                sendErrorResponse(response, "not_displaying", "Given item is not displaying currently.");
                return;
            }

            ExistingItemUpdater.updateItemState(connection, requestData.itemKey, newItemState);
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        
        ResponseData data = new ResponseData();
        data.itemState = newItemState.value();
        
        sendJsonResponse(response, data);
    }
}
