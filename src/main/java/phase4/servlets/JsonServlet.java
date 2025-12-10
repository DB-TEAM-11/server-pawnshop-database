package phase4.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.PlayerKeyByToken;
import phase4.utils.SQLConnector;

public class JsonServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private class ErrorResponseData {
        String code;
        String error;
    }
    
    protected Gson gson = new Gson();
    
    protected void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        sendJsonResponse(response, 200, data);
    }
    
    protected void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().append(gson.toJson(data)).close();
    }
    
    protected void sendErrorResponse(HttpServletResponse response, String code, String error) throws IOException {
        sendErrorResponse(response, 400, code, error);
    }
    
    protected void sendErrorResponse(HttpServletResponse response, int status, String code, String error) throws IOException {
        ErrorResponseData data = new ErrorResponseData();
        data.code = code;
        data.error = error;
        
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().append(gson.toJson(data)).close();
    }
    
    protected void sendStackTrace(HttpServletResponse response, Exception e) throws IOException {
        response.setContentType("text/plain");
        response.setStatus(500);

        StringBuffer buffer = new StringBuffer("Unexpected exception" + e.getClass().getName() + " | Traceback:");
        buffer.append("\n----------------------------------------\n");
        StringWriter errorWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(errorWriter));
        buffer.append(errorWriter.toString());

        System.err.println(buffer);

        PrintWriter writer = response.getWriter();
        writer.println(buffer);
        writer.close();
    }
    
    protected void sendEmptyJsonResponse(HttpServletResponse response) throws IOException {
        sendEmptyJsonResponse(response, 200);
    }
    
    protected void sendEmptyJsonResponse(HttpServletResponse response, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().append("{}").close();
    }
    
    protected void sendEmptyResponse(HttpServletResponse response) {
        sendEmptyResponse(response, 200);
    }
    
    protected void sendEmptyResponse(HttpServletResponse response, int status) {
        response.setContentType("application/json");
        response.setStatus(status);
    }

    protected int authenticateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            sendErrorResponse(response, 401, "no_token", "No session token");
            return 0;
        }
        if (authorization.length() != 70 || !authorization.startsWith("Token ")) {
            sendErrorResponse(response, 401, "malformed_token", "Got a malformed session token");
            return 0;
        }
        
        String sessionToken = authorization.substring(6);
        
        int playerKey;
        try (Connection connection = SQLConnector.connect()) {
            try {
                playerKey = PlayerKeyByToken.getPlayerKey(connection, sessionToken);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, 401, "invalid_token", "Given session token is invalid.");
                return 0;
            }
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return 0;
        }

        return playerKey;
    }
}
