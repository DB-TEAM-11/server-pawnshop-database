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
        TodaysEvent[] newsList;
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
            } catch (NotASuchRowException e){
                sendErrorResponse(response, "no_game_session", "No game session exists.");
                return;
            }
            responseData.newsList = TodaysEvent.getTodaysEvent(connection, gameSessionKey);
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        sendJsonResponse(response, responseData);
    }

}
