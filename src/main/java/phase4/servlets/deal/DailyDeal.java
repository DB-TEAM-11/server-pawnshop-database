package phase4.servlets.deal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import phase4.constants.AffectedPrice;
import phase4.constants.Grade;
import phase4.exceptions.NotASuchRowException;
import phase4.queries.CustomerProperty;
import phase4.queries.InsertDealRecord;
import phase4.queries.InsertExistingItem;
import phase4.queries.ItemCatalog;
import phase4.queries.PlayerInfo;
import phase4.queries.RandomCustomerWithDetail;
import phase4.queries.TodaysEvent;
import phase4.servlets.JsonServlet;
import phase4.utils.SQLConnector;


@WebServlet("/deal/loadOrGenerateDailyDeals")
public class DailyDeal extends JsonServlet {
    private static final long serialVersionUID = 1L;
    
    private static class ResponseData {
        static class Deal {
            int drcKey;
            int askingPrice;
            int purchasePrice;
            int appraisedPrice;
            int itemKey;
            int itemCatalogKey;
            int foundGrade;
            int foundFlawEa;
            int foundAuthenticity;
            int customerKey;
            double revealedFraud;
            double revealedWellCollect;
            double revealedClumsy;
        }

        Deal[] dailyDeals;
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int playerKey = authenticateUser(request, response);
        if (playerKey <= 0) {
            return;
        }

        Random random = new Random();
        ArrayList<ResponseData.Deal> deals = new ArrayList<>();
        
        PlayerInfo playerInfo;
        int askingPricePercent = 100, appraisedPricePercent = 100;
        try (Connection connection = SQLConnector.connect()) {
            try {
                playerInfo = PlayerInfo.getPlayerInfo(connection, playerKey);
            } catch (NotASuchRowException e) {
                sendErrorResponse(response, "no_game_session", "No game session exists.");
                return;
            }
            
            for (TodaysEvent event: TodaysEvent.getTodaysEvent(connection, playerInfo.gameSessionKey)) {
                if (event.affectedPrice == AffectedPrice.ASKING.value()) {
                    askingPricePercent += event.amount;
                }
                if (event.affectedPrice == AffectedPrice.APPRAISED.value()) {
                    appraisedPricePercent += event.amount;
                }
            }

            ItemCatalog itemCatalog;
            RandomCustomerWithDetail customer;
            CustomerProperty customerProperty;
            ResponseData.Deal deal;
            double gradeBase;

            Grade grade;
            int flawEa;
            float suspiciousFlawAura;
            boolean authenticity;
            int basePrice, askingPrice, appraisedPrice;
            int itemKey, dealRecordKey;
            for (int i = 0; i < 3; i++) {
                itemCatalog = ItemCatalog.getRandomItem(connection);
                customer = RandomCustomerWithDetail.getRandomCustomersWithDetails(connection, 1)[0];
                customerProperty = CustomerProperty.getCustomerProperty(connection, customer.customerKey);
                deal = new ResponseData.Deal();
                gradeBase = random.nextDouble();

                if (gradeBase < customerProperty.normalProbability) {
                    grade = Grade.NORMAL;
                } else if (gradeBase < customerProperty.rareProbability + customerProperty.normalProbability) {
                    grade = Grade.RARE;
                } else if (gradeBase < customerProperty.uniqueProbability + customerProperty.rareProbability + customerProperty.normalProbability) {
                    grade = Grade.UNIQUE;
                } else {
                    grade = Grade.LEGENDARY;
                }
                flawEa = (int)(customerProperty.flawBase + 4 * random.nextDouble());
                suspiciousFlawAura = random.nextFloat();
                authenticity = random.nextDouble() > customerProperty.fakeProbability;
                
                basePrice = (int)(
                    itemCatalog.basePrice
                    * (1 - 0.02 * (flawEa))
                    * (1 - 0.3 * customerProperty.fakeProbability)
                    * (1 / 3 + 0.1 * Grade.priceMultiplier[grade.value()])
                    * (1 + 0.25 * customer.fraud)
                    * (0.9 + 0.2 * customer.wellCollect)
                );
                askingPrice = (int)(basePrice * (askingPricePercent / 100.0));
                appraisedPrice = (int)(
                    askingPrice
                    * (1 - flawEa * 0.05)
                    * Grade.priceMultiplier[grade.value()]
                    * (appraisedPricePercent / 100.0)
                );

                itemKey = InsertExistingItem.insertItem(
                    connection, playerInfo.gameSessionKey, itemCatalog.itemCatalogKey,
                    grade.value(), flawEa, suspiciousFlawAura, authenticity ? 'Y' : 'N'
                );
                dealRecordKey = InsertDealRecord.insertDealRecord(
                    connection, playerInfo.gameSessionKey, customer.customerKey,
                    itemKey, askingPrice, 0, appraisedPrice
                );

                deal.drcKey = dealRecordKey;
                deal.askingPrice = askingPrice;
                deal.purchasePrice = 0;
                deal.appraisedPrice = appraisedPrice;
                deal.itemKey = itemKey;
                deal.itemCatalogKey = itemCatalog.itemCatalogKey;
                deal.foundGrade = -1;
                deal.foundFlawEa = -1;
                deal.foundAuthenticity = -1;
                deal.customerKey = customer.customerKey;
                deal.revealedFraud = -1;
                deal.revealedWellCollect = -1;
                deal.revealedClumsy = -1;

                deals.add(deal);
            }
        } catch (SQLException e) {
            sendStackTrace(response, e);
            return;
        }
        
        ResponseData data = new ResponseData();
        data.dailyDeals = deals.toArray(new ResponseData.Deal[0]);
        
        sendJsonResponse(response, data);
    }
}
