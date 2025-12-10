package phase4.servlets.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.CustomerInfo;
import phase4.queries.MoneyUpdater;
import phase4.queries.PlayerInfo;
import phase4.queries.RevealCustomerInfo;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;


@WebServlet("/customer/reveal")
public class Reveal extends JsonServlet {
    private static final long serialVersionUID = 1L;
    
    private class RequestData {
        int customerKey;
        String attribute;
    }

    private class ResponseData {
        String attribute;
        int value;
        int leftMoney;
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestData requestData;
        int playerKey = authenticateUser(request, response);
        if (playerKey <= 0) {
            return;
        }
        
        try {
            requestData = gson.fromJson(request.getReader().lines().collect(Collectors.joining("\n")), RequestData.class);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, "invalid_data", "Received malformed data");
            return;
        }
        
        CustomerInfo customerInfo;
        int leftMoney;
        try (Connection connection = SQLConnector.connect()) {
            try {
                MoneyUpdater.subtractMoney(connection, playerKey, 50);
                customerInfo = CustomerInfo.getCustomerInfo(connection, requestData.customerKey);
                leftMoney = MoneyUpdater.getMoney(connection, playerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, 404, "no display data", "no display data in this game session");
                return;
            }       
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }

        ResponseData responseData = new ResponseData();
        switch (requestData.attribute) {
            case "FRAUD":
                responseData.attribute = "FRAUD";
                responseData.value = (int)customerInfo.fraud;
                break;
            case "WELL_COLLECT":
                responseData.attribute = "WELL_COLLECT";
                responseData.value = (int)customerInfo.wellCollect;
                break;
            case "CLUMSY":
                responseData.attribute = "CLUMSY";
                responseData.value = (int)customerInfo.clumsy;
                break;
            default:
                sendErrorResponse(response, "invalid_attribute", "Invalid attribute specified");
                return;
        }
        responseData.leftMoney = leftMoney;

        sendJsonResponse(response, responseData);
    }
 
}
