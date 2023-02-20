package i5.las2peer.apiTestModel;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import i5.las2peer.apiTestModel.exception.TestCaseNotFoundException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Represents one test case of a test suite.
 */
public class TestCase implements Serializable {

    /**
     * Id of the test case.
     * Different versions of the same test case have the same id.
     */
    private int id;

    /**
     * Id of the test model, that the test case belongs to.
     * Different versions of the same test case have different model ids.
     */
    private int modelId;

    /**
     * Name of the test case.
     */
    private String name;

    /**
     * List of requests that are part of the test case.
     */
    private List<TestRequest> requests;

    private String status = "none";

    public TestCase(String name, List<TestRequest> requests) {
        this.name = name;
        this.requests = requests;
    }

    /**
     * Creates a TestCase object given a JSONObject representing it.
     *
     * @param testCase JSON representation of the test case.
     */
    public TestCase(JSONObject testCase) {
        this.id = (int) ((long) testCase.get("id"));
        this.name = (String) testCase.get("name");

        // get requests
        JSONArray requestsJSON = (JSONArray) testCase.get("requests");
        this.requests = new ArrayList<>();
        for (Object requestObj : requestsJSON) {
            JSONObject request = (JSONObject) requestObj;
            // create TestRequest from JSONObject
            this.requests.add(new TestRequest(request, this.id));
        }
    }

    /**
     * Loads the test case with the given testCaseId from the model with the given modelId from the database.
     *
     * @param connection
     * @param testCaseId Id of the test case to load.
     * @param modelId    Id of the model, that the test case belongs to.
     * @throws SQLException If the test case could not be found.
     */
    public TestCase(Connection connection, int testCaseId, int modelId) throws SQLException {
        this.id = testCaseId;
        this.modelId = modelId;

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM TestCase WHERE testCaseId=? AND modelId=?;");
        statement.setInt(1, this.id);
        statement.setInt(2, this.modelId);

        // execute query
        ResultSet queryResult = statement.executeQuery();

        // check for results
        if (queryResult.next()) {
            this.name = queryResult.getString("name");
            this.loadTestRequests(connection);
        } else {
            // there does not exist a test case with the given id in the database
            throw new TestCaseNotFoundException(id);
        }

        statement.close();
    }

    /**
     * Stores the current TestCase (and its requests) to the database.
     *
     * @param connection
     * @param modelId    Id of the TestModel, that the test case belongs to.
     * @throws SQLException If storing the test case or one of its requests failed.
     */
    public void persist(Connection connection, int modelId) throws SQLException {
        this.modelId = modelId;

        PreparedStatement statement = connection.prepareStatement("INSERT INTO TestCase (testCaseId, modelId, name) VALUES (?,?,?);");
        statement.setInt(1, this.id);
        statement.setInt(2, this.modelId);
        statement.setString(3, this.name);
        statement.executeUpdate();
        statement.close();

        // persist requests
        for (TestRequest request : this.requests) {
            request.persist(connection, this.modelId);
        }
    }


    /**
     * Loads the test requests that are part of the current test case from the database.
     *
     * @param connection
     * @throws SQLException
     */
    private void loadTestRequests(Connection connection) throws SQLException {
        this.requests = new ArrayList<>();

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM TestRequest WHERE modelId=? AND testCaseId=?;");
        statement.setInt(1, this.modelId);
        statement.setInt(2, this.id);

        // execute query
        ResultSet queryResult = statement.executeQuery();
        while (queryResult.next()) {
            int testRequestId = queryResult.getInt("testRequestId");
            this.requests.add(new TestRequest(connection, testRequestId, this.modelId, this.id));
        }

        statement.close();
    }

    public JSONObject toJSONObject() {
        JSONObject testCase = new JSONObject();

        testCase.put("id", this.id);
        testCase.put("name", this.name);

        JSONArray requestsJSON = new JSONArray();
        for (TestRequest request : this.requests) {
            requestsJSON.add(request.toJSONObject());
        }
        testCase.put("requests", requestsJSON);
        testCase.put("status", this.status);

        return testCase;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public List<TestRequest> getRequests() {
        return this.requests;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean contentEquals(TestCase other) {
        if (this.requests.size() != other.getRequests().size()) return false;
        for (TestRequest request : this.getRequests()) {
            if (!other.containsContentEqualRequest(request)) return false;
        }
        return true;
    }

    private boolean containsContentEqualRequest(TestRequest request) {
        for (TestRequest r : this.requests) {
            if (r.contentEquals(request)) return true;
        }
        return false;
    }
}
