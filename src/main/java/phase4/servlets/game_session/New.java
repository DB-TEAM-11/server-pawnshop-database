package phase4.servlets.game_session;

import java.io.IOException;
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
import phase4.queries.GameSessionCreator;
import phase4.queries.PlayerInfo;
import phase4.queries.PlayerKeyByToken;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;


@WebServlet("/game-session/new")
public class New extends JsonServlet {
	private static final long serialVersionUID = 1L;

	private class RequestData {
        String nickname;
        String shopName;
    }
    
    private class ResponseData {
        int dayCount;
        int money;
        int personalDebt;
        int pawnshopDebt;
        int unlockedShowcaseCount;
        String nickname;
        String shopName;
    }
    
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z]{1,30}");
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            
            RequestData requestData;
            try {
                requestData = gson.fromJson(request.getReader().lines().collect(Collectors.joining("\n")), RequestData.class);
            } catch (JsonSyntaxException e) {
                sendErrorResponse(response, "invalid_data", "Received malformed data");
                return;
            }
            
            if (requestData.nickname == null) {
                sendErrorResponse(response, "no_nickname", "No nickname supplied.");
                return;
            }
            if (requestData.shopName == null) {
                sendErrorResponse(response, "no_shopname", "No shopname supplied.");
                return;
            }
            if (!NAME_PATTERN.matcher(requestData.nickname).matches()) {
                sendErrorResponse(response, "invalid_nickname", "Given nickname is invalid.");
                return;
            }
            if (!NAME_PATTERN.matcher(requestData.shopName).matches()) {
                sendErrorResponse(response, "invalid_shopname", "Given shopname is invalid.");
                return;
            }
            
            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
                if (playerInfo.gameEndDayCount <= 0) {
                    sendErrorResponse(response, "not_finished", "The last game session is not finished.");
                    return;
                }
            } catch (NotASuchRowException e) {
            }
            
            GameSessionCreator.createGameSession(connection, playerKey, requestData.nickname, requestData.shopName);
            
            playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
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
