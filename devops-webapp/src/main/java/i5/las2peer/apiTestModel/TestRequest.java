package i5.las2peer.apiTestModel;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import i5.las2peer.apiTestModel.exception.TestRequestNotFoundException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.json.simple.JSONValue;

/**
 * Represents one request of a test case.
 */
public class TestRequest implements Serializable {

    /**
     * Id of the request.
     * Different versions of the same request have the same id.
     */
    private int id;

    /**
     * Id of the test model, that the request belongs to.
     * Different versions of the same request have different model ids.
     */
    private int modelId;

    /**
     * Id of the test case, that the request belongs to.
     */
    private int testCaseId;

    /**
     * Request type, e.g., "GET".
     */
    private String type;

    /**
     * Request URL.
     */
    private String url;

    private JSONObject pathParams = null;

    /**
     * Agent that was chosen for authorization.
     * -1 means no authorization.
     */
    private int authSelectedAgent = -1;

    /**
     * Request body.
     */
    private String body = null;

    /**
     * Response assertions.
     */
    private List<RequestAssertion> assertions;

    private String lastResponse = null;

    public TestRequest(String type, String url, List<RequestAssertion> assertions) {
        this.type = type;
        this.url = url;
        this.assertions = assertions;
        this.pathParams = new JSONObject();
    }

    public TestRequest(String type, String url, JSONObject pathParams, int authSelectedAgent, String body, List<RequestAssertion> assertions) {
        this.type = type;
        this.url = url;
        this.pathParams = pathParams;
        this.authSelectedAgent = authSelectedAgent;
        this.body = body;
        this.assertions = assertions;
    }

    /**
     * Creates a TestRequest object given a JSONObject representing it and the id of the test case.
     *
     * @param request    JSON representation of the request.
     * @param testCaseId Id of the test case that the request belongs to.
     */
    public TestRequest(JSONObject request, int testCaseId) {
        this.id = (int) ((long) request.get("id"));
        this.testCaseId = testCaseId;
        this.type = (String) request.get("type");
        this.url = (String) request.get("url");
        if (request.containsKey("body")) {
            this.body = (String) request.get("body");
        }

        this.pathParams = (JSONObject) JSONValue.parse((String) request.get("pathParams"));

        // check if auth is enabled
        if (request.containsKey("auth")) {
            JSONObject auth = (JSONObject) request.get("auth");
            if (auth.containsKey("selectedAgent")) {
                this.authSelectedAgent = (int) ((long) auth.get("selectedAgent"));
            }
        }

        // get assertions
        this.assertions = new ArrayList<>();
        JSONArray assertionsJSON = (JSONArray) request.get("assertions");
        for (Object assertionJSON : assertionsJSON) {
            JSONObject assertion = (JSONObject) assertionJSON;
            // create RequestAssertion from the JSONObject
            this.assertions.add(RequestAssertion.fromJSONObject(assertion, this.id));
        }
    }

    /**
     * Loads the request with the given testRequestId from the model with the given modelId from the database.
     *
     * @param connection
     * @param testRequestId Id of the request to load.
     * @param modelId       Id of the model, that the request belongs to.
     * @param testCaseId    Id of the test case, that the request belongs to.
     * @throws SQLException If the test request could not be found.
     */
    public TestRequest(Connection connection, int testRequestId, int modelId, int testCaseId) throws SQLException {
        this.id = testRequestId;
        this.modelId = modelId;
        this.testCaseId = testCaseId;

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM TestRequest WHERE testRequestId=? AND modelId=?;");
        statement.setInt(1, this.id);
        statement.setInt(2, this.modelId);

        // execute query
        ResultSet queryResult = statement.executeQuery();

        // check for results
        if (queryResult.next()) {
            this.type = queryResult.getString("type");
            this.url = queryResult.getString("url");
            this.authSelectedAgent = queryResult.getInt("authSelectedAgent");
            if (queryResult.wasNull()) this.authSelectedAgent = -1;
            this.body = queryResult.getString("body");
            this.pathParams = (JSONObject) JSONValue.parse(queryResult.getString("pathParams"));
            this.loadAssertions(connection);
        } else {
            // there does not exist a test request with the given id in the database
            throw new TestRequestNotFoundException(testRequestId);
        }

        statement.close();
    }

    /**
     * Stores the current TestRequest (and its assertions) to the database.
     *
     * @param connection
     * @param modelId    Id of the model, that the request belongs to.
     * @throws SQLException If storing the request or one of its assertions failed.
     */
    public void persist(Connection connection, int modelId) throws SQLException {
        this.modelId = modelId;

        String statementStr = "INSERT INTO TestRequest (testRequestId, modelId, testCaseId, type, url, body, pathParams) VALUES (?,?,?,?,?,?,?);";
        if (this.isAuthEnabled()) {
            statementStr = "INSERT INTO TestRequest (testRequestId, modelId, testCaseId, type, url, body, pathParams, authSelectedAgent) VALUES (?,?,?,?,?,?,?,?);";
        }
        PreparedStatement statement = connection.prepareStatement(statementStr);
        statement.setInt(1, this.id);
        statement.setInt(2, this.modelId);
        statement.setInt(3, this.testCaseId);
        statement.setString(4, this.type);
        statement.setString(5, this.url);
        statement.setString(6, this.body);
        statement.setString(7, this.pathParams.toJSONString());
        if (this.isAuthEnabled()) {
            statement.setInt(8, this.authSelectedAgent);
        }
        statement.executeUpdate();
        statement.close();

        // persist assertions
        for (RequestAssertion assertion : this.assertions) {
            assertion.persist(connection, this.modelId);
        }
    }

    /**
     * Loads the assertions corresponding to the current request from the database.
     *
     * @param connection
     * @throws SQLException
     */
    private void loadAssertions(Connection connection) throws SQLException {
        this.assertions = new ArrayList<>();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM RequestAssertion WHERE modelId=? AND testRequestId=?;");
        statement.setInt(1, this.modelId);
        statement.setInt(2, this.id);

        // execute query
        ResultSet queryResult = statement.executeQuery();
        while (queryResult.next()) {
            int requestAssertionId = queryResult.getInt("requestAssertionId");
            this.assertions.add(RequestAssertion.loadFromDatabase(connection, requestAssertionId, this.modelId, this.id));
        }

        statement.close();
    }

    public JSONObject toJSONObject() {
        JSONObject request = new JSONObject();

        request.put("id", this.id);
        request.put("type", this.type);
        request.put("url", this.url);
        if (this.body == null) {
            request.put("body", "");
        } else {
            request.put("body", this.body);
        }

        request.put("pathParams", this.pathParams.toJSONString());

        if (this.lastResponse != null) {
            request.put("lastResponse", this.lastResponse);
        }

        JSONObject auth = new JSONObject();
        if (this.isAuthEnabled()) {
            auth.put("selectedAgent", this.authSelectedAgent);
        }
        request.put("auth", auth);

        JSONArray assertionsJSON = new JSONArray();
        for (RequestAssertion assertion : this.assertions) {
            assertionsJSON.add(assertion.toJSONObject());
        }
        request.put("assertions", assertionsJSON);

        return request;
    }

    /**
     * Whether authorization is enabled for the request.
     *
     * @return Whether authorization is enabled for the request.
     */
    public boolean isAuthEnabled() {
        return this.authSelectedAgent != -1;
    }

    public int getId() {
        return this.id;
    }

    public String getUrl() {
        return this.url;
    }

    public String getType() {
        return this.type;
    }

    public int getAgent() {
        return this.authSelectedAgent;
    }

    public String getBody() {
        return this.body;
    }

    public List<RequestAssertion> getAssertions() {
        return this.assertions;
    }

    public void setLastResponse(String lastResponse) {
        this.lastResponse = lastResponse;
    }

    public JSONObject getPathParams() {
        return this.pathParams;
    }

    public boolean contentEquals(TestRequest other) {
        if (!this.getType().equals(other.getType())) return false;
        if (!this.getUrl().equals(other.getUrl())) return false;
        JSONObject pathParamsOther = other.getPathParams();
        if (this.pathParams.keySet().size() != pathParamsOther.keySet().size()) return false;
        for (Object key : this.pathParams.keySet()) {
            if (!pathParamsOther.containsKey(key)) return false;
            if (!this.pathParams.get(key).equals(pathParamsOther.get(key))) return false;
        }
        if (this.authSelectedAgent != other.authSelectedAgent) return false;
        if (!this.body.equals(other.getBody())) return false;
        for (RequestAssertion assertion : this.getAssertions()) {
            if (!other.containsContentEqualAssertion(assertion)) return false;
        }
        return true;
    }

    private boolean containsContentEqualAssertion(RequestAssertion assertion) {
        for (RequestAssertion a : this.assertions) {
            if (a.contentEquals(assertion)) return true;
        }
        return false;
    }
}
