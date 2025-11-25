package phase4.servlets.player;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.GameSessionGetter;
import phase4.queries.SessionTokenSetter;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;

@WebServlet("/player/login")
public class Login extends JsonServlet {
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
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z]{1,30}");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("[!-~]+");
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestData requestData;
        try {
            requestData = gson.fromJson(request.getReader().lines().collect(Collectors.joining("\n")), RequestData.class);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, "invalid_data", "Received malformed data");
            return;
        }
        
        if (!USERNAME_PATTERN.matcher(requestData.playerId).matches()) {
            sendErrorResponse(response, "invalid_username", "The username is invalid.");
            return;
        }
        if (!PASSWORD_PATTERN.matcher(requestData.password).matches()) {
            sendErrorResponse(response, "invalid_password", "The password is invalid.");
            return;
        }
        
        ResponseData responseData = new ResponseData();
        responseData.hasGameSession = "Y";
        try (Connection connection = SQLConnector.connect()) {
            responseData.sessionToken = SessionTokenSetter.setNewSessionToken(connection, requestData.playerId, requestData.password);
            try {
                GameSessionGetter.getGameSessionBySessionToken(connection, responseData.sessionToken);
            } catch (NotASuchRowException e) {
                responseData.hasGameSession = "N";
            }
        } catch (NotASuchRowException e) {
            sendErrorResponse(response, 401, "no_such_user", "The specified user is not exists.");
            return;
        } catch (SQLException e) {
            sendStackTrace(response, "SQLException", e);
            return;
        }
        
        sendJsonResponse(response, responseData);
    }
}
