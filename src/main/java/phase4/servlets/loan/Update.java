package phase4.servlets.loan;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.GameSessionUpdater;
import phase4.queries.NotFoundItemCategory;
import phase4.queries.PawnshopDebt;
import phase4.queries.PersonalDebt;
import phase4.queries.PlayerInfo;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;


@WebServlet("/loan/update")
public class Update extends JsonServlet {
    private static final long serialVersionUID = 1L;
    
    private class RequestData {
        String debtType;
        int amount;
    }
    
    private class ResponseData {
        class WorldRecord {
            String playerId;
            String nickname;
            String pawnshopName;
            int gameEndDayCount;
            String gameEndDate;
        }
        
        String debtType;
        int leftDebtAmount;
        int leftMoney;
        String isGameCleared;
        String[] notFoundCategoryList;
        WorldRecord worldRecord = new WorldRecord();
    }

    private final SimpleDateFormat gameEndDateFormat = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ");
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int playerKey = authenticateUser(request, response);
        if (playerKey <= 0) {
            return;
        }

        RequestData requestData;
        try {
            requestData = gson.fromJson(request.getReader().lines().collect(Collectors.joining("\n")), RequestData.class);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, "invalid_data", "Received malformed data");
            return;
        }
        if (requestData.amount == 0) {
            sendErrorResponse(response, "no_amount", "No amount supplied, or the amount is 0.");
            return;
        }
        if (requestData.debtType == null) {
            sendErrorResponse(response, "no_type", "No type supplied.");
            return;
        }
        
        PlayerInfo playerInfo;
        boolean gameIsCleared;
        String[] notFoundItemCategories = new String[0];
        try (Connection connection = SQLConnector.connect()) {
            connection.setAutoCommit(false);

            switch (requestData.debtType) {
                case "PERSONAL":
                    PersonalDebt.addToPersonalDebt(connection, playerKey, requestData.amount);
                    break;
                case "PAWNSHOP":
                    PawnshopDebt.addToShopDebt(connection, playerKey, requestData.amount);
                    break;
                default:
                    sendErrorResponse(response, "invalid_type", "Given type is invalid.");
                    break;
            }

            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_game_session", "No game session exists.");
                return;
            }
            
            gameIsCleared = playerInfo.pawnshopDebt < 0 && playerInfo.personalDebt < 0;
            if (gameIsCleared) {
                GameSessionUpdater.setGameEnd(connection, playerInfo.gameSessionKey, true);
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
                notFoundItemCategories = NotFoundItemCategory.getNotFoundItemCategories(connection, playerKey);
            }

            connection.commit();
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        
        ResponseData data = new ResponseData();
        data.debtType = requestData.debtType;
        switch (requestData.debtType) {
            case "PERSONAL":
                data.leftDebtAmount = playerInfo.personalDebt;
                break;
            case "PAWNSHOP":
                data.leftDebtAmount = playerInfo.pawnshopDebt;
                break;
        }
        data.leftMoney = playerInfo.money;
        data.isGameCleared = gameIsCleared ? "Y" : "N";
        data.notFoundCategoryList = notFoundItemCategories;
        data.worldRecord.playerId = playerInfo.playerId;
        data.worldRecord.nickname = playerInfo.nickname;
        data.worldRecord.pawnshopName = playerInfo.shopName;
        data.worldRecord.gameEndDayCount = playerInfo.gameEndDayCount;
        data.worldRecord.gameEndDate = gameEndDateFormat.format(playerInfo.gameEndDate);
        
        sendJsonResponse(response, data);
    }
}
