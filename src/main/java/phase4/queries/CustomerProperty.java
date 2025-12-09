package phase4.queries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import phase4.exceptions.NotASuchRowException;

public class CustomerProperty {
    private static final String QUERY = "SELECT (10 * CLUMSY) FLAW_BASE, (10 + (50 * WELL_COLLECT)) LEGENDARY_P, (25 + (2 * WELL_COLLECT)) UNIQUE_P, (30 - (22 * WELL_COLLECT)) RARE_P, (35 - (30 * WELL_COLLECT)) NORMAL_P, FAKE_P, (1 - FAKE_P) GENIUE_P FROM ( SELECT CC.*, (10 + 90 * FRAUD) FAKE_P FROM CUSTOMER_CATALOG CC WHERE CUSTOMER_KEY = ? )";

    public float flawBase;
    public float legendaryProbability;
    public float uniqueProbability;
    public float rareProbability;
    public float normalProbability;
    public float fakeProbability;
    public float geniueProbability;

    private CustomerProperty(
        float flawBase,
        float legendaryProbability,
        float uniqueProbability,
        float rareProbability,
        float normalProbability,
        float fakeProbability,
        float geniueProbability
    ) {
        this.flawBase = flawBase;
        this.legendaryProbability = legendaryProbability;
        this.uniqueProbability = uniqueProbability;
        this.rareProbability = rareProbability;
        this.normalProbability = normalProbability;
        this.fakeProbability = fakeProbability;
        this.geniueProbability = geniueProbability;
    }

    public static CustomerProperty getCustomerProperty(Connection connection, int customerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(QUERY)) {
            statement.setInt(1, customerId);
            try (ResultSet queryResult = statement.executeQuery()) {
                if (!queryResult.next()) {
                    throw new NotASuchRowException();
                }
        
                float fakeProbability = queryResult.getFloat(6);
                float geniueProbability = queryResult.getFloat(7);
                
                // FAKE_P는 0~100 범위의 퍼센트 값이므로, GENIUE_P = (1 - FAKE_P)는 음수가 됨
                // 1-55 = -54
                // 따라서 100 - FAKE_P로 보정
                // 100-55 = 45
                if (geniueProbability < 0) {
                    geniueProbability = 100 - fakeProbability;
                }
        
                return new CustomerProperty(
                    queryResult.getFloat(1),
                    queryResult.getFloat(2),
                    queryResult.getFloat(3),
                    queryResult.getFloat(4),
                    queryResult.getFloat(5),
                    fakeProbability,
                    geniueProbability
                );
            }
        }
    }
}
