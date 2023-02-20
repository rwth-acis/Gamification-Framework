package i5.las2peer.apiTestModel;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import i5.las2peer.apiTestModel.exception.AssertionNotFoundException;
import org.json.simple.JSONObject;

/**
 * Assertion on the body of the response.
 */
public class BodyAssertion extends RequestAssertion implements Serializable {

    public static final int ASSERTION_TYPE_ID = 1;

    /**
     * First operator of the assertion.
     */
    private BodyAssertionOperator operator;

    public BodyAssertion(int id, int testRequestId, JSONObject operator) {
        super(id, testRequestId, ASSERTION_TYPE_ID);

        this.operator = new BodyAssertionOperator(operator);
    }

    public BodyAssertion(int id, int testRequestId, int modelId, BodyAssertionOperator operator) {
        super(id, testRequestId, ASSERTION_TYPE_ID, modelId);

        this.operator = operator;
    }

    public BodyAssertion(BodyAssertionOperator operator) {
        super(0, 0, ASSERTION_TYPE_ID);
        this.operator = operator;
    }

    /**
     * Loads the body assertion with the given id from the database.
     *
     * @param connection
     * @param modelId            Id of the model, that the assertion belongs to.
     * @param testRequestId      Id of the request, that the assertion belongs to.
     * @param requestAssertionId Id of the assertion to load.
     * @return
     * @throws SQLException
     */
    public static BodyAssertion loadFromDatabase(Connection connection, int modelId, int testRequestId, int requestAssertionId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM BodyAssertion WHERE modelId=? AND requestAssertionId=?;");
        statement.setInt(1, modelId);
        statement.setInt(2, requestAssertionId);

        // execute query
        ResultSet queryResult = statement.executeQuery();

        // check for results
        if (queryResult.next()) {
            int operatorId = queryResult.getInt("operatorId");

            statement.close();

            BodyAssertionOperator operator = BodyAssertionOperator.loadFromDatabase(connection, operatorId, modelId);

            return new BodyAssertion(requestAssertionId, testRequestId, modelId, operator);
        } else {
            statement.close();

            // there does not exist a body assertion with the given id in the database
            throw new AssertionNotFoundException(requestAssertionId);
        }
    }

    /**
     * Stores the part of the assertion that is specific for the BodyAssertion to the database.
     */
    public void persist(Connection connection, int modelId) throws SQLException {
        super.persist(connection, modelId);

        PreparedStatement statement = connection.prepareStatement("INSERT INTO BodyAssertion (modelId, requestAssertionId, operatorId) VALUES (?,?,?);");
        statement.setInt(1, modelId);
        statement.setInt(2, this.id);
        statement.setInt(3, this.operator.getId());

        statement.executeUpdate();
        statement.close();

        // also persist the operator
        operator.persist(connection, modelId);
    }

    public JSONObject toJSONObject() {
        JSONObject assertion = super.toJSONObject();
        assertion.put("operator", this.operator.toJSONObject());
        return assertion;
    }

    public BodyAssertionOperator getOperator() {
        return this.operator;
    }
}
