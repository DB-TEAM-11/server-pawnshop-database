package phase3.queries;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import phase3.exceptions.NotASuchRowException;

public class CustomerProperty {
    private static final String QUERY = "SELECT (10 * CLUMSY) FLAW_BASE, (15 + (65 * WELL_COLLECT)) LEGENDARY_P, (20 + PROBABILITY_BASE) UNIQUE_P, (30 + PROBABILITY_BASE) RARE_P, (35 + PROBABILITY_BASE) NORMAL_P, FAKE_P, (1 - FAKE_P) GENIUE_P FROM ( SELECT CC.*, (65 * (1 - WELL_COLLECT) / 3) PROBABILITY_BASE, (10 + 90 * FRAUD) FAKE_P FROM CUSTOMER_CATALOG CC WHERE CUSTOMER_KEY = %d )";

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
        Statement statement = connection.createStatement();
        ResultSet queryResult = statement.executeQuery(String.format(QUERY, customerId));
        if (!queryResult.next()) {
            throw new NotASuchRowException();
        }

        CustomerProperty customerProperty = new CustomerProperty(
            queryResult.getFloat(1),
            queryResult.getFloat(2),
            queryResult.getFloat(3),
            queryResult.getFloat(4),
            queryResult.getFloat(5),
            queryResult.getFloat(6),
            queryResult.getFloat(7)
        );

        statement.close();
        queryResult.close();

        return customerProperty;
    }
}
