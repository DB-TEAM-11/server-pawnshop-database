package phase4.servlets.item;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.queries.CustomerProperty;
import phase4.queries.MoneyUpdater;
import phase4.queries.SellerGetter;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

@WebServlet("/item/getHints")
public class GetHints extends JsonServlet {
	private static final long serialVersionUID = 1L;
	
	private class RequestData {
        int itemKey;
    }
	
	private class ResponseData {
		String hintName;
		float hintValue;
		int leftMoney;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestData requestData;
		Random random = new Random();
        int itemKey, playerKey = authenticateUser(request, response);
        int sellerKey, leftMoney = 0;;
        float hintValue = 0;;
        String hintName = "";
        
        
		try {
            requestData = gson.fromJson(request.getReader().lines().collect(Collectors.joining("\n")), RequestData.class);
            itemKey = requestData.itemKey;
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, "invalid_data", "Received malformed data");
            return;
        }
         
        try (Connection connection = SQLConnector.connect()) {
            try {
                sellerKey = SellerGetter.GetSellerKey(connection, itemKey);
            } catch (SQLException e1) {
                sendErrorResponse(response, "not_seller_key", "Given item key is not mapped seller.");
            	return;
			}
            try {
            	int randInt = random.nextInt(4);
            	switch (randInt) {
	            	case 0: {
	                	hintValue = CustomerProperty.getCustomerProperty(connection, sellerKey).normalProbability;
	                	hintName = "일반 확률";
	            		break;
	            	}
	            	case 1: {
	                	hintValue = CustomerProperty.getCustomerProperty(connection, sellerKey).rareProbability;
	                	hintName = "레어 확률";
	            		break;
	            	}
	            	case 2: {
	                	hintValue = CustomerProperty.getCustomerProperty(connection, sellerKey).uniqueProbability;
	                	hintName = "유니크 확률";
	            		break;
	            	}
	            	case 3: {
	                	hintValue = CustomerProperty.getCustomerProperty(connection, sellerKey).legendaryProbability;
	                	hintName = "레전더리 확률";
	            		break;
	            	}
            	}
            } catch (SQLException e2) {
                sendErrorResponse(response, "not_hintValue", "Can not calculate the item's hintValue from this seller key");
            	return;
            }                
            try {
            	MoneyUpdater.subtractMoney(connection, playerKey, 10);
            } catch (SQLException e2) {
                sendErrorResponse(response, "not_selling", "Given item is not selling currently.");
            	return;
            }
            try {
            	MoneyUpdater.subtractMoney(connection, playerKey, 10);
            } catch (SQLException e2) {
                sendErrorResponse(response, "failed update", "failed update Money.");
            	return;
            }
            try {
            	leftMoney = MoneyUpdater.getMoney(connection, playerKey);
            } catch (SQLException e2) {
                sendErrorResponse(response, "failed get", "failed get money.");
            	return;
            }
        } catch (SQLException eSql) {
        	sendStackTrace(response, eSql);
            return;
        }
        
        
		
		ResponseData responseData = new ResponseData();
		responseData.hintName = hintName;
		responseData.hintValue = hintValue;
		responseData.leftMoney = leftMoney;
		
        sendJsonResponse(response, responseData);
	}

}
