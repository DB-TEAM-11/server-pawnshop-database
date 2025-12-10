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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int playerKey = authenticateUser(request, response);
        if (playerKey <= 0) {
            return;
        }
        
        PlayerInfo playerInfo;
        try (Connection connection = SQLConnector.connect()) {
            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_game_session", "No game session exists.");
                return;
            }
        } catch (SQLException e) {
            sendStackTrace(response, e);
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
