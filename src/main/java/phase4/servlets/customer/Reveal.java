package phase4.servlets.customer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.MoneyUpdater;
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
		RevealCustomerInfo customerInfo;
    }
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestData requestData;
		
		try {
            requestData = gson.fromJson(request.getReader().lines().collect(Collectors.joining("\n")), RequestData.class);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, "invalid_data", "Received malformed data");
            return;
        }
		
		
		ResponseData responseData = new ResponseData();
		int playerKey = authenticateUser(request, response);
        if (playerKey <= 0) {
            return;
        }
        
        try (Connection connection = SQLConnector.connect()) {
        	
        	try {
        		MoneyUpdater.subtractMoney(connection, playerKey, 50);
        		int leftMoney = MoneyUpdater.getMoney(connection, playerKey);
        		responseData.customerInfo = RevealCustomerInfo.getCustomerInfo(
        				connection, 
        				requestData.customerKey,
        				requestData.attribute,
        				leftMoney
        			);
        	} catch (NotASuchRowException e) {
        		sendErrorResponse(response, 404, "no display data", "no display data in this game session");
                return;
        	}       
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        sendJsonResponse(response, responseData.customerInfo);
	}
 
}
