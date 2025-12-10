package phase4.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.queries.WorldRecord;
import phase4.utils.SQLConnector;


@WebServlet("/worldRecords")
public class WorldRecords extends JsonServlet {
    private static final long serialVersionUID = 1L;

    private class ResponseData {
        WorldRecord[] worldRecords;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ResponseData responseData = new ResponseData();
        
        try (Connection connection = SQLConnector.connect()) {
            responseData.worldRecords = WorldRecord.retrieveWorldRecord(connection);
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }

        sendJsonResponse(response, responseData);
    }
}
