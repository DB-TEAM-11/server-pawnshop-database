package phase4.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

public class JsonServlet extends HttpServlet {
    private class ErrorResponseData {
        String code;
        String message;
    }
    
    protected Gson gson = new Gson();
    
    public void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        sendJsonResponse(response, 200, data);
    }
    
    public void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().append(gson.toJson(data)).close();
    }
    
    public void sendErrorResponse(HttpServletResponse response, String code, String message) throws IOException {
        sendErrorResponse(response, 400, code, message);
    }
    
    public void sendErrorResponse(HttpServletResponse response, int status, String code, String message) throws IOException {
        ErrorResponseData data = new ErrorResponseData();
        data.code = code;
        data.message = message;
        
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().append(gson.toJson(data)).close();
    }
    
    public void sendStackTrace(HttpServletResponse response, String exceptionClassName, Exception e) throws IOException {
        response.setContentType("text/plain");
        response.setStatus(500);
        PrintWriter writer = response.getWriter();
        writer.println("Unexpected" + exceptionClassName + "occured. Traceback:");
        writer.println("----------------------------------------");
        e.printStackTrace(writer);
        writer.close();
    }
    
    public void sendEmptyJsonResponse(HttpServletResponse response) throws IOException {
        sendEmptyJsonResponse(response, 200);
    }
    
    public void sendEmptyJsonResponse(HttpServletResponse response, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
        response.getWriter().append("{}").close();
    }
    
    public void sendEmptyResponse(HttpServletResponse response) throws IOException {
        sendEmptyResponse(response, 200);
    }
    
    public void sendEmptyResponse(HttpServletResponse response, int status) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);
    }
}
