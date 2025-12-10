package phase4.servlets.deal;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.queries.DealRecordByDrcKey;
import phase4.queries.DealRecordUpdater;
import phase4.queries.ExistingItemUpdater;
import phase4.queries.ItemCatalog;
import phase4.queries.MoneyUpdater;
import phase4.queries.TodaysEvent;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.coyote.CloseNowException;

import com.google.gson.JsonSyntaxException;

@WebServlet("/deal/action/")
public class DealAction extends JsonServlet {
    private static final long serialVersionUID = 1L;
    
    private class RequestData {
        int drcKey;
        int actionLevel;  
        String actionType;
    }
    
    private class ResponseData {
        int totalPurchasePrice;
        int totalAppraisedPrice;
        int foundGrade;
        int foundFlawEa;
        int foundAuthenticity;
        int leftMoney;
        String changedPurchasedPriceByAction;   // 구매가 상승/하락분
        String changedAppraisedPriceByAction;   // 감정가 상승/하락분
    }
    
    private void showItemAuthenticationScreen(
            Connection connection,
            HttpServletResponse response,
            ResponseData responseData,
            int playerKey,
            int drcKey
            ) {
        int money;
        try {
            money = MoneyUpdater.getMoney(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        if (money < 200) {
            try {
                sendErrorResponse(response, "not_enough_money", "Not enough money");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            return;
        }
        
        DealRecordByDrcKey dealRecord;
        try {
            dealRecord = DealRecordByDrcKey.getDealRecordByDrcKey(connection, drcKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        
        
        try {
            ExistingItemUpdater.updateAuthenticityFound(connection, dealRecord.itemKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        
        int oldPurchasePrice = dealRecord.purchasePrice;
        int oldAppraisedPrice = dealRecord.appraisedPrice;
        int newPurchasePrice = dealRecord.purchasePrice;
        int newAppraisedPrice = dealRecord.appraisedPrice;
        
        
        if (!dealRecord.authenticity) {
            // 이벤트 수치 적용
            ItemCatalog itemCatalog;
            try {
                itemCatalog = ItemCatalog.getItemByKey(connection, dealRecord.itemCatalogKey);
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
            double eventMultiplier;
            try {
                eventMultiplier = getEventMultiplier(connection, itemCatalog.categoryKey, playerKey);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            
            newPurchasePrice = (int)(dealRecord.askingPrice * (1 - dealRecord.foundFlawEa * 0.05) * 0.5 * eventMultiplier);
            double gradeMultiplier = getGradeMultiplier(dealRecord.foundGrade);
            newAppraisedPrice = (int)(dealRecord.askingPrice * (1 - dealRecord.foundFlawEa * 0.05) * gradeMultiplier * 0.8 * eventMultiplier);
            
            int changedPurchase = newPurchasePrice - oldPurchasePrice;
            int changedAppraise = newAppraisedPrice - oldAppraisedPrice;
            if (changedPurchase < 0) {
                responseData.changedAppraisedPriceByAction = "" + changedPurchase;
            } else {
                responseData.changedAppraisedPriceByAction = "" + changedPurchase;
            }
            if (changedAppraise < 0) {
                responseData.changedPurchasedPriceByAction = "" + changedAppraise;
            } else {
                responseData.changedPurchasedPriceByAction = "" + changedAppraise;
            }
            responseData.totalPurchasePrice = newPurchasePrice;
            responseData.totalAppraisedPrice = newAppraisedPrice;
            responseData.foundGrade = 0;
            responseData.foundFlawEa = 0;
            responseData.foundAuthenticity = (dealRecord.authenticity ? 1 : 0);            
            try {
                DealRecordUpdater.updatePrices(connection, drcKey, newPurchasePrice, newAppraisedPrice);
            } catch (SQLException e) {
                e.printStackTrace();
                return;            
            }
        }
        
        try {
            MoneyUpdater.subtractMoney(connection, playerKey, 200);
            responseData.leftMoney = MoneyUpdater.getMoney(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }
   
    
    
    private void showGradeFindScreen(
            Connection connection, 
            HttpServletResponse response,
            ResponseData responseData,
            int playerKey, 
            int drcKey,
            int level) {
        switch (level) {
            case 1:
            performGradeAppraisal(connection, response, responseData, playerKey, drcKey, 1, 20);
            return;
            case 2:
            performGradeAppraisal(connection, response, responseData, playerKey, drcKey, 1, 30);
            return;
            case 3:
            performGradeAppraisal(connection, response, responseData, playerKey, drcKey, 1, 50);
            return;
            case 4:
            return;
        }
    }
    
    private void performGradeAppraisal(
            Connection connection, 
            HttpServletResponse response,
            ResponseData responseData,
            int playerKey, 
            int drcKey,
            int maxGrade, 
            int cost
        ) {
        int money;
        try {
            money = MoneyUpdater.getMoney(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        if (money < cost) {
            try {
                sendErrorResponse(response, "not_enough_money", "Not enough money");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }            
            return;
        }
        
        DealRecordByDrcKey dealRecord;
        try {
            dealRecord = DealRecordByDrcKey.getDealRecordByDrcKey(connection, drcKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
       
        
        Random random = new Random();
        int resultGrade = determineGradeByAppraisal(maxGrade, dealRecord.grade, random);
        
        
        // 이벤트 수치 적용
        ItemCatalog itemCatalog;
        try {
            itemCatalog = ItemCatalog.getItemByKey(connection, dealRecord.itemCatalogKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        double eventMultiplier;
        try {
            eventMultiplier = getEventMultiplier(connection, itemCatalog.categoryKey, playerKey);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        double gradeMultiplier = getGradeMultiplier(resultGrade);
        int newAppraisedPrice = (int)(dealRecord.askingPrice * (1 - dealRecord.foundFlawEa * 0.05) * gradeMultiplier * eventMultiplier);
        if (dealRecord.isAuthenticityFound && !dealRecord.authenticity) {
            newAppraisedPrice = (int)(newAppraisedPrice * 0.8);
        }
        
        
        int oldAppraisedPrice = dealRecord.appraisedPrice;
        int changedAppraise = newAppraisedPrice - oldAppraisedPrice;
        if (changedAppraise < 0) {
            responseData.changedPurchasedPriceByAction = "" + changedAppraise;
        } else {
            responseData.changedPurchasedPriceByAction = "" + changedAppraise;
        }
        responseData.changedPurchasedPriceByAction = "0";
        responseData.totalPurchasePrice = dealRecord.purchasePrice;
        responseData.totalAppraisedPrice = newAppraisedPrice;
        responseData.foundGrade = resultGrade;
        responseData.foundFlawEa = 0;
        responseData.foundAuthenticity = 0;   
        
        try {
            DealRecordUpdater.updatePrices(connection, drcKey, dealRecord.purchasePrice, newAppraisedPrice);
            MoneyUpdater.subtractMoney(connection, playerKey, cost);
            responseData.leftMoney = MoneyUpdater.getMoney(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        
    }
    
    private int determineGradeByAppraisal(int maxGrade, int realGrade, Random random) {
        // maxGrade: 1=레어, 2=유니크, 3=레전더리
        // realGrade: 0=일반, 1=레어, 2=유니크, 3=레전더리
        
        double rand = random.nextDouble();
        int result;
        
        if (maxGrade == 3) { // 레전더리 감정
            if (rand < 0.10) result = 0; // 일반 10%
            else if (rand < 0.30) result = 1; // 레어 20%
            else if (rand < 0.60) result = 2; // 유니크 30%
            else result = 3; // 레전더리 40%
        } else if (maxGrade == 2) { // 유니크 감정
            if (rand < 0.20) result = 0; // 일반 20%
            else if (rand < 0.50) result = 1; // 레어 30%
            else result = 2; // 유니크 50%
        } else if (maxGrade == 1) { // 레어 감정
            if (rand < 0.30) result = 0; // 일반 30%
            else result = 1; // 레어 70%
        } else {
            result = 0; // 기본값
        }
        
        // 실제 등급보다 높게 나올 수 없음
        return Math.min(result, realGrade);
    }

    
    private void showFlawFind(
            Connection connection, 
            HttpServletResponse response,
            ResponseData responseData,
            int playerKey, 
            int drcKey,
            int level) {
        switch (level) {
            case 1:
            performFlawFind(connection, response, responseData, playerKey, drcKey, 1, 20);
            return;
            case 2:
            performFlawFind(connection, response, responseData, playerKey, drcKey, 4, 60);
            return;
            case 3:
            performFlawFind(connection, response, responseData, playerKey, drcKey, 7, 100);
            return;
            case 4:
            return;
        }
    }

    
    private void performFlawFind(
            Connection connection,
            HttpServletResponse response,
            ResponseData responseData,
            int playerKey,
            int drcKey,
            int maxFind, 
            int cost
        ) {
        int money;
        try {
            money = MoneyUpdater.getMoney(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        if (money < cost) {
            try {
                sendErrorResponse(response, "not_enough_money", "Not enough money");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            return;
        }
        
        DealRecordByDrcKey dealRecord;
        try {
            dealRecord = DealRecordByDrcKey.getDealRecordByDrcKey(connection, drcKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
                
        // 최대로 찾을 수 있는 개수. 남은 거만큼만 찾을 수 있음
        int maxPossibleFlaws = Math.min(dealRecord.flawEa - dealRecord.foundFlawEa, maxFind);
        
        // 업데이트 될 찾은 흠 개수
        int newFoundFlawEa = dealRecord.foundFlawEa + maxPossibleFlaws;
        
        try {
            ExistingItemUpdater.addToFoundFlaw(connection, dealRecord.itemKey, maxPossibleFlaws);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        
        // 이벤트 수치 적용
        ItemCatalog itemCatalog;
        try {
            itemCatalog = ItemCatalog.getItemByKey(connection, dealRecord.itemCatalogKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        double eventMultiplier;
        try {
            eventMultiplier = getEventMultiplier(connection, itemCatalog.categoryKey, playerKey);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        int newPurchasePrice = (int)(dealRecord.askingPrice * (1 - newFoundFlawEa * 0.05) * eventMultiplier);
        
        if (dealRecord.isAuthenticityFound && !dealRecord.authenticity) {
            newPurchasePrice = (int)(newPurchasePrice * 0.5);
        }
        
        double gradeMultiplier = getGradeMultiplier(dealRecord.foundGrade);
        int newAppraisedPrice = (int)(dealRecord.askingPrice * (1 - newFoundFlawEa * 0.05) * gradeMultiplier * eventMultiplier);
        
        if (dealRecord.isAuthenticityFound && !dealRecord.authenticity) {
            newAppraisedPrice = (int)(newAppraisedPrice * 0.8);
        }
        
        int oldPurchasePrice = dealRecord.purchasePrice;
        int oldAppraisedPrice = dealRecord.appraisedPrice;

        int changedPurchase = newPurchasePrice - oldPurchasePrice;
        int changedAppraise = newAppraisedPrice - oldAppraisedPrice;
        if (changedPurchase < 0) {
            responseData.changedAppraisedPriceByAction = "" + changedPurchase;
        } else {
            responseData.changedAppraisedPriceByAction = "" + changedPurchase;
        }
        if (changedAppraise < 0) {
            responseData.changedPurchasedPriceByAction = "" + changedAppraise;
        } else {
        responseData.totalPurchasePrice = newPurchasePrice;
        responseData.totalAppraisedPrice = newAppraisedPrice;
        responseData.foundGrade = 0;
        responseData.foundFlawEa = newFoundFlawEa;
        responseData.foundAuthenticity = 0;
        
        try {
            DealRecordUpdater.updatePrices(connection, drcKey, newPurchasePrice, newAppraisedPrice);
            MoneyUpdater.subtractMoney(connection, playerKey, cost);
            responseData.leftMoney = MoneyUpdater.getMoney(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        }  
    }
    
    private double getGradeMultiplier(int grade) {
        switch (grade) {
            case 0: return 1.0;
            case 1: return 1.2;
            case 2: return 1.5;
            case 3: return 1.7;
            default: return 1.0;
        }
    }
    
    private double getEventMultiplier(
            Connection connection,
            int itemCategoryKey, 
            int playerKey
            ) throws IOException {
        TodaysEvent[] events;
        try {
            events = TodaysEvent.getTodaysEvent(connection, playerKey);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new CloseNowException();
        }
        
        double multiplier = 1.0;
        for (TodaysEvent event : events) {
            if (event.categoryKey == itemCategoryKey) {
                double change = event.affectedPrice / 100.0;
                if (event.plusMinus == 1) {
                    multiplier += change;
                } else {
                    multiplier -= change;
                }
            }
        }
        return multiplier;
    }
    

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestData requestData;
        int playerKey = authenticateUser(request, response);
        
        try {
            requestData = gson.fromJson(request.getReader().lines().collect(Collectors.joining("\n")), RequestData.class);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, "invalid_data", "Received malformed data");
            return;
        }
         
        ResponseData responseData = new ResponseData();

        try (Connection connection = SQLConnector.connect()) {
            switch(requestData.actionType) {
            case "FINDFLAW": {
                showFlawFind(connection, response, responseData, playerKey, requestData.drcKey, requestData.actionLevel);
                break;
            }
            case "AUTHCHECK": {
                showItemAuthenticationScreen(connection, response, responseData, playerKey, requestData.drcKey);
                break;
            }
            case "APPRAISE": {
                showGradeFindScreen(connection, response, responseData, playerKey, requestData.drcKey, requestData.actionLevel);
                break;
            }
        }
        } catch (SQLException eSql) {
            sendStackTrace(response, eSql);
            return;
        }
        
        
        sendJsonResponse(response, responseData);
    }

}
