package phase4.servlets.player;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.SessionTokenSetter;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;


@WebServlet("/player/logout")
public class Logout extends JsonServlet {
	private static final long serialVersionUID = 1L;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int playerKey = authenticateUser(request, response);
        if (playerKey <= 0) {
            return;
        }
        
        try (Connection connection = SQLConnector.connect()) {
            SessionTokenSetter.removeSessionToken(connection, playerKey);
        } catch (NotASuchRowException e) {
            sendErrorResponse(response, 500, "unknown", "Unknown error occured.");
            return;
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }

        sendEmptyJsonResponse(response);
    }
}
