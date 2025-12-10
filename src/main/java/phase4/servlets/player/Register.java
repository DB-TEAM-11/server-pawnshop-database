package phase4.servlets.player;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.queries.AuthenticationCreator;
import phase4.servlets.JsonServlet;
import phase4.utils.PasswordHasher;
import phase4.utils.SQLConnector;


@WebServlet("/player/register")
public class Register extends JsonServlet {
    private static final long serialVersionUID = 1L;

    private class RequestData {
        String playerId;
        String password;
        
        @Override
        public String toString() {
            return String.format("ID: %s | PW: %s", playerId, password);
        }
    }
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-zA-Z]{1,30}");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("[!-~]+");
    
    Gson gson = new Gson();
    
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
        
        String salt = PasswordHasher.getSalt16();
        String hashedPassword = PasswordHasher.calculateHashedPassword(requestData.password, salt);
        String hashedPasswordWithSalt = hashedPassword + ";" + salt;
        
        try (Connection connection = SQLConnector.connect()) {
            AuthenticationCreator.createAuthentication(connection, requestData.playerId, hashedPasswordWithSalt);
        } catch (SQLIntegrityConstraintViolationException e) {
            sendErrorResponse(response, "already_exists", "The username already exists.");
            return;
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        
        sendEmptyJsonResponse(response);
    }
}
