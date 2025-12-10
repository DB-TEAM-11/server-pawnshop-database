package phase4.servlets.catalog;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.StaticCustomer;
import phase4.queries.StaticItem;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;

@WebServlet("/catalog/initialData")
public class InitialData extends JsonServlet {
    private static final long serialVersionUID = 1L;

    private class ResponseData {
        StaticItem[] itemCatalogs;
        StaticCustomer[] customerCatalogs;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ResponseData responseData = new ResponseData();
        
        try (Connection connection = SQLConnector.connect()) {
            try {
                responseData.itemCatalogs = StaticItem.loadAllItems(connection);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, 401, "no item catalogs", "The item catalogs are not exists.");
            }
            try {
                responseData.customerCatalogs = StaticCustomer.loadAllCustomers(connection);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, 401, "no customer catalogs", "The customer catalogs are not exists.");
            }
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        sendJsonResponse(response, responseData);
    }
}
