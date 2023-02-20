package i5.las2peer.apiTestModel;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import i5.las2peer.apiTestModel.exception.AssertionNotFoundException;
import org.json.simple.JSONObject;

/**
 * Represents an assertion which can either be a StatusCodeAssertion
 * or BodyAssertion.
 */
public class RequestAssertion implements Serializable {

    /**
     * Id of the assertion.
     * Different versions of the same assertion have the same id.
     */
    protected int id;

    /**
     * Id of the test model, that the assertion belongs to.
     * Different versions of the same assertion have different model ids.
     */
    private int modelId;

    /**
     * Id of the request, that the assertion belongs to.
     */
    private int testRequestId;

    /**
     * Type of the assertion.
     * 0: StatusCodeAssertion
     * 1: BodyAssertion
     */
    private int assertionType;

    private String status = "none";
    private String errorMessage = null;

    public RequestAssertion(int assertionType) {
        this.assertionType = assertionType;
    }

    public RequestAssertion(int id, int testRequestId, int assertionType) {
        this.id = id;
        this.testRequestId = testRequestId;
        this.assertionType = assertionType;
    }

    public RequestAssertion(int id, int testRequestId, int assertionType, int modelId) {
        this.id = id;
        this.testRequestId = testRequestId;
        this.assertionType = assertionType;
        this.modelId = modelId;
    }

    /**
     * Creates the correct RequestAssertion (either StatusCodeAssertion or
     * BodyAssertion) for the given JSONObject.
     *
     * @param assertion     JSON representation of the assertion.
     * @param testRequestId Id of the request, that the assertion belongs to.
     * @return RequestAssertion object.
     */
    public static RequestAssertion fromJSONObject(JSONObject assertion, int testRequestId) {
        int id = (int) ((long) assertion.get("id"));
        int assertionType = (int) ((long) assertion.get("assertionType"));

        JSONObject operator = (JSONObject) assertion.get("operator");

        if (assertionType == 0) {
            return new StatusCodeAssertion(id, testRequestId, operator);
        } else {
            return new BodyAssertion(id, testRequestId, operator);
        }
    }

    /**
     * Loads the assertion with the given id from the database.
     *
     * @param connection
     * @param id            Id of the assertion to load.
     * @param modelId       Id of the model that the assertion belongs to.
     * @param testRequestId Id of the request that the assertion belongs to.
     * @return StatusCodeAssertion or BodyAssertion object
     * @throws SQLException
     */
    public static RequestAssertion loadFromDatabase(Connection connection, int id, int modelId, int testRequestId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM RequestAssertion WHERE requestAssertionId=? AND modelId=? AND testRequestId=?;");
        statement.setInt(1, id);
        statement.setInt(2, modelId);
        statement.setInt(3, testRequestId);

        // execute query
        ResultSet queryResult = statement.executeQuery();

        // check for results
        if (queryResult.next()) {
            int assertionType = queryResult.getInt("assertionType");

            statement.close();

            if (assertionType == StatusCodeAssertion.ASSERTION_TYPE_ID) {
                return StatusCodeAssertion.loadFromDatabase(connection, modelId, testRequestId, id);
            } else if (assertionType == BodyAssertion.ASSERTION_TYPE_ID) {
                return BodyAssertion.loadFromDatabase(connection, modelId, testRequestId, id);
            } else {
                throw new IllegalArgumentException("Unknown assertion type: " + assertionType);
            }
        } else {
            statement.close();

            // there does not exist a request assertion with the given id in the database
            throw new AssertionNotFoundException(id);
        }
    }

    /**
     * Stores the current assertion to the database.
     *
     * @param connection
     * @param modelId    Id of the model, that the assertion belongs to.
     * @throws SQLException If storing the assertion failed.
     */
    public void persist(Connection connection, int modelId) throws SQLException {
        this.modelId = modelId;

        PreparedStatement statement = connection.prepareStatement("INSERT INTO RequestAssertion (requestAssertionId, modelId, testRequestId, assertionType) VALUES (?,?,?,?);");
        statement.setInt(1, this.id);
        statement.setInt(2, this.modelId);
        statement.setInt(3, this.testRequestId);
        statement.setInt(4, this.assertionType);

        statement.executeUpdate();
        statement.close();
    }

    public JSONObject toJSONObject() {
        JSONObject assertion = new JSONObject();

        assertion.put("id", this.id);
        assertion.put("assertionType", this.assertionType);
        assertion.put("status", this.status);
        if (this.errorMessage != null) {
            assertion.put("errorMessage", this.errorMessage);
        }

        return assertion;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getAssertionType() {
        return assertionType;
    }

    public boolean contentEquals(RequestAssertion assertion) {
        return false;
    }
}
