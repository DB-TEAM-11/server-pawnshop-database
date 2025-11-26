package phase4.servlets.game_session;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.PlayerInfo;
import phase4.queries.PlayerKeyByToken;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;


@WebServlet("/game-session/latest")
public class Latest extends JsonServlet {
	private static final long serialVersionUID = 1L;
    
    private class ResponseData {
        int dayCount;
        int money;
        int personalDebt;
        int pawnshopDebt;
        int unlockedShowcaseCount;
        String nickname;
        String shopName;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            sendErrorResponse(response, 401, "no_token", "No session token");
            return;
        }
        if (authorization.length() != 70 || !authorization.startsWith("Token ")) {
            sendErrorResponse(response, 401, "malformed_token", "Got a malformed session token");
            return;
        }
        
        String sessionToken = authorization.substring(6);
        
        PlayerInfo playerInfo;
        try (Connection connection = SQLConnector.connect()) {
            int playerKey;
            try {
                playerKey = PlayerKeyByToken.getPlayerKey(connection, sessionToken);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, 401, "invalid_token", "Given session token is invalid.");
                return;
            }
            
            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_game_session", "No game session exists.");
                return;
            }
        } catch (SQLException e) {
            sendStackTrace(response, "SQLException", e);
            return;
        }
        
        ResponseData data = new ResponseData();
        data.dayCount = playerInfo.dayCount;
        data.money = playerInfo.money;
        data.personalDebt = playerInfo.personalDebt;
        data.pawnshopDebt = playerInfo.pawnshopDebt;
        data.unlockedShowcaseCount = playerInfo.unlockedShowcaseCount;
        data.nickname = playerInfo.nickname;
        data.shopName = playerInfo.shopName;
        
        sendJsonResponse(response, data);
    }
}
