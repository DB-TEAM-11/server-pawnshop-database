package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import phase4.exceptions.NotASuchRowException;

public class ItemInDisplay {
    private static final String QUERY = "SELECT GSID.DISPLAY_POS, DR.ASKING_PRICE, DR.PURCHASE_PRICE, DR.APPRAISED_PRICE, DR.BOUGHT_DATE, CC.CUSTOMER_NAME, EI.FOUND_GRADE, EI.FOUND_FLAW_EA, EI.IS_AUTHENTICITY_FOUND, EI.AUTHENTICITY, EI.ITEM_STATE, EI.ITEM_KEY, EI.ITEM_CATALOG_KEY FROM GAME_SESSION_ITEM_DISPLAY GSID JOIN DEAL_RECORD DR ON GSID.ITEM_KEY = DR.ITEM_KEY JOIN CUSTOMER_CATALOG CC ON DR.SELLER_KEY = CC.CUSTOMER_KEY JOIN EXISTING_ITEM EI ON GSID.ITEM_KEY = EI.ITEM_KEY WHERE GSID.GAME_SESSION_KEY = ?";
    
    
    public int displayPositionKey;
    public int askingPrice;
    public int purchasePrice;
    public int appraisedPrice;
    public int boughtDate;
    public String sellerName;
    public int foundGrade; 
    public int foundFlawEa;
    public int authenticity; // 서버에서 처리isAuthenticityFound가 Y면은 (진위 여부 판단 됨) 실제 Authenticity의 값을 그대로 주면 되고 ("Y" = 1(진품), "N" = 0(가품)) isAuthenticityFound가 N이면은(진위여부 판단 안 됨. 모르는 상태)  -1(미확정)
    public int itemState;
    public int itemKey;
    public int itemCatalogKey; 
    
    private ItemInDisplay(
         int displayPositionKey,
         int askingPrice,
         int purchasePrice,
         int appraisedPrice,
         int boughtDate,
         String sellerName,
         int foundGrade,
         int foundFlawEa,
         int authenticity,
         int itemState,
         int itemKey,
         int itemCatalogKey
    ) {
        this.displayPositionKey = displayPositionKey;
        this.askingPrice = askingPrice;
        this.purchasePrice = purchasePrice;
        this.appraisedPrice = appraisedPrice;
        this.boughtDate = boughtDate;
        this.sellerName = sellerName;
        this.foundGrade = foundGrade;
        this.foundFlawEa = foundFlawEa;
        this.authenticity = authenticity;
        this.itemState = itemState;
        this.itemKey = itemKey;
        this.itemCatalogKey = itemCatalogKey;

    }
    
    private static int parseAuthenticity(String isFound, String auth) {
        if (isFound.equals("N")) {
            return -1;
        }
        return auth.equals("Y") ? 1 : 0;
    }

    public static ItemInDisplay[] getItemInDisplay(Connection connection, int gameSessionKey) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, gameSessionKey);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
                ArrayList<ItemInDisplay> itemInDisplay = new ArrayList<ItemInDisplay>();
                do {
                    itemInDisplay.add(new ItemInDisplay(
                        queryResult.getInt(1), // displayPositionKey
                        queryResult.getInt(2), // askingPrice
                        queryResult.getInt(3), // purchasePrice
                        queryResult.getInt(4), // appraisedPrice
                        queryResult.getInt(5), //boughtDate
                        queryResult.getString(6),// sellerName
                        queryResult.getInt(7), // foundFlawEa
                        queryResult.getInt(8), // authenticity
                        parseAuthenticity( // authenticity
                            queryResult.getString(9),    // isAuthenticityFound Y/N
                            queryResult.getString(10)    // authenticity Y/N
                        ),
                        queryResult.getInt(11), //itemState
                        queryResult.getInt(12), //itemKey
                        queryResult.getInt(13) //itemCatalogKey
                    ));
                } while (queryResult.next());
                return itemInDisplay.toArray(new ItemInDisplay[0]);
            }
        }
    }
}
