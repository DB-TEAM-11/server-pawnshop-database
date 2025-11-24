package phase4.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.GameSessionGetter;
import phase4.queries.SessionToken;
import phase4.utils.SQLConnector;

@WebServlet("/login")
public class Login extends HttpServlet {
    private class RequestData {
        String playerId;
        String password;
        
        @Override
        public String toString() {
            return String.format("ID: %s | PW: %s", playerId, password);
        }
    }
    
    private class ResponseData {
        String sessionToken;
        String hasGameSession;
        
        @Override
        public String toString() {
            return String.format("Session Token: %s | PW: %b", sessionToken, hasGameSession);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        RequestData requestData = gson.fromJson(request.getReader().lines().collect(Collectors.joining("\n")), RequestData.class);
        
        ResponseData responseData = new ResponseData();
        responseData.hasGameSession = "Y";
        try (Connection connection = SQLConnector.connect()) {
            responseData.sessionToken = SessionToken.generateSessionToken(connection, requestData.playerId, requestData.password);
            try {
                GameSessionGetter.getGameSessionBySessionToken(connection, responseData.sessionToken);
            } catch (NotASuchRowException e) {
                responseData.hasGameSession = "N";
            }
        } catch (SQLException e) {
            response.setContentType("text/plain");
            response.setStatus(500);
            PrintWriter writer = response.getWriter();
            writer.println("Unexpected SQLException occured:");
            writer.println("----------------------------------------");
            e.printStackTrace(writer);
            writer.close();
        }
        String responseJson = gson.toJson(responseData);

        response.setContentType("application/json");
        response.setStatus(200);
        response.getWriter().append(responseJson).close();
    }
}
