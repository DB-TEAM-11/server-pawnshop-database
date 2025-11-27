package phase4.servlets.news;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.GameSessionGetter;
import phase4.queries.TodaysEvent;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/news/current")
public class Current extends JsonServlet {
	private static final long serialVersionUID = 1L;
	
	private class ResponseData {
		TodaysEvent[] todayEvents;
	}
       

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ResponseData responseData = new ResponseData();
		int playerKey = authenticateUser(request, response);
        if (playerKey <= 0) {
            return;
        }
        try (Connection connection = SQLConnector.connect()) {
        	int gameSessionKey;
        	try {
        		gameSessionKey = GameSessionGetter.getGameSessionByPlayerKey(connection, playerKey);
        	}
        	catch (NotASuchRowException e){
        		sendErrorResponse(response, 404, "this player hasn't game session", "no game session");
                return;
        	}
        	try {
        		responseData.todayEvents = TodaysEvent.getTodaysEvent(connection, gameSessionKey);
        	} catch (NotASuchRowException e) {
        		sendErrorResponse(response, 404, "no display data", "no display data in this game session");
                return;
        	}       
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        sendJsonResponse(response, responseData);
	}

}
