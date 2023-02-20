package i5.las2peer.apiTestModel;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import i5.las2peer.apiTestModel.exception.AssertionNotFoundException;
import org.json.simple.JSONObject;

/**
 * Assertion on the status code of the response.
 */
public class StatusCodeAssertion extends RequestAssertion implements Serializable {

    public static final int ASSERTION_TYPE_ID = 0;
    public static final int COMPARISON_OPERATOR_EQUALS = 0;

    private int comparisonOperator;

    /**
     * Status code that gets compared to the response status code.
     */
    private int statusCodeValue;

    public StatusCodeAssertion(int comparisonOperator, int statusCodeValue) {
        super(ASSERTION_TYPE_ID);

        this.comparisonOperator = comparisonOperator;
        this.statusCodeValue = statusCodeValue;
    }

    public StatusCodeAssertion(int id, int testRequestId, JSONObject operator) {
        super(id, testRequestId, ASSERTION_TYPE_ID);

        this.comparisonOperator = (int) ((long) operator.get("id"));
        this.statusCodeValue = (int) ((long) ((JSONObject) operator.get("input")).get("value"));
    }

    public StatusCodeAssertion(int id, int testRequestId, int modelId, int comparisonOperator, int statusCodeValue) {
        super(id, testRequestId, ASSERTION_TYPE_ID, modelId);

        this.comparisonOperator = comparisonOperator;
        this.statusCodeValue = statusCodeValue;
    }

    /**
     * Loads the status code assertion from the database.
     *
     * @param connection
     * @param modelId            Id of the model, that the assertion belongs to.
     * @param testRequestId      Id of the request, that the assertion belongs to.
     * @param requestAssertionId Id of the assertion.
     * @return
     * @throws SQLException If assertion could not be found.
     */
    public static StatusCodeAssertion loadFromDatabase(Connection connection, int modelId, int testRequestId, int requestAssertionId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM StatusCodeAssertion WHERE modelId=? AND requestAssertionId=?;");
        statement.setInt(1, modelId);
        statement.setInt(2, requestAssertionId);

        // execute query
        ResultSet queryResult = statement.executeQuery();

        // check for results
        if (queryResult.next()) {
            int comparisonOperator = queryResult.getInt("comparisonOperator");
            int statusCodeValue = queryResult.getInt("statusCodeValue");

            statement.close();

            return new StatusCodeAssertion(requestAssertionId, testRequestId, modelId, comparisonOperator, statusCodeValue);
        } else {
            statement.close();

            // there does not exist a status code assertion with the given id in the database
            throw new AssertionNotFoundException(requestAssertionId);
        }
    }

    /**
     * Stores the part of the assertion that is specific for the StatusCodeAssertion to the database.
     */
    public void persist(Connection connection, int modelId) throws SQLException {
        super.persist(connection, modelId);

        PreparedStatement statement = connection.prepareStatement("INSERT INTO StatusCodeAssertion (modelId, requestAssertionId, comparisonOperator, statusCodeValue) VALUES (?,?,?,?);");
        statement.setInt(1, modelId);
        statement.setInt(2, this.id);
        statement.setInt(3, this.comparisonOperator);
        statement.setInt(4, this.statusCodeValue);

        statement.executeUpdate();
        statement.close();
    }

    public JSONObject toJSONObject() {
        JSONObject assertion = super.toJSONObject();

        JSONObject operatorJSON = new JSONObject();
        operatorJSON.put("id", this.comparisonOperator);
        JSONObject operatorInputJSON = new JSONObject();
        operatorInputJSON.put("value", this.statusCodeValue);
        operatorJSON.put("input", operatorInputJSON);
        assertion.put("operator", operatorJSON);

        return assertion;
    }

    public int getComparisonOperator() {
        return this.comparisonOperator;
    }

    public int getStatusCodeValue() {
        return this.statusCodeValue;
    }

    public boolean contentEquals(RequestAssertion other) {
        if (!(other instanceof StatusCodeAssertion)) return false;
        StatusCodeAssertion otherStatusCodeAssertion = (StatusCodeAssertion) other;
        return this.comparisonOperator == otherStatusCodeAssertion.getComparisonOperator() && this.statusCodeValue == otherStatusCodeAssertion.getStatusCodeValue();
    }
}
