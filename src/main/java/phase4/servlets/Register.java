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
import phase4.queries.AuthenticationCreator;
import phase4.queries.SessionToken;
import phase4.utils.SQLConnector;

@WebServlet("/register")
public class Register extends HttpServlet {
    private class RequestData {
        String playerId;
        String password;
        
        @Override
        public String toString() {
            return String.format("ID: %s | PW: %s", playerId, password);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        RequestData requestData = gson.fromJson(request.getReader().lines().collect(Collectors.joining("\n")), RequestData.class);
        
        try (Connection connection = SQLConnector.connect()) {
            String salt = SessionToken.getSalt16();
            String hashedPassword = SessionToken.calculateHashedPassword(requestData.password, salt);
            String hashedPasswordWithSalt = hashedPassword + ";" + salt;
            AuthenticationCreator.createAuthentication(connection, requestData.playerId, hashedPasswordWithSalt);
        } catch (SQLException e) {
            response.setContentType("text/plain");
            response.setStatus(500);
            PrintWriter writer = response.getWriter();
            writer.println("Unexpected SQLException occured:");
            writer.println("----------------------------------------");
            e.printStackTrace(writer);
            writer.close();
        }

        response.setContentType("application/json");
        response.setStatus(200);
        response.getWriter().append("{}").close();
    }
}
