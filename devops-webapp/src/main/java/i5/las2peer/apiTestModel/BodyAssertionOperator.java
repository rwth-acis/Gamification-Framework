package i5.las2peer.apiTestModel;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import i5.las2peer.apiTestModel.exception.AssertionOperatorNotFoundException;
import org.json.simple.JSONObject;

/**
 * Represents a part of a BodyAssertion.
 */
public class BodyAssertionOperator implements Serializable {

    /**
     * Id of the operator.
     * Different versions of the same operator have the same id.
     */
    private int id;

    private int operatorId;

    /**
     * Id of the test model, that the operator belongs to.
     * Different versions of the same operator have different model ids.
     */
    private int modelId;

    /**
     * Id of the input for the operator, e.g., no input, input field or a data type.
     */
    private int inputType = -1;

    /**
     * Value that has been entered or selected.
     */
    private String inputValue = null;

    /**
     * The following operator, if there exists one.
     */
    private BodyAssertionOperator followedByOperator = null;

    /**
     * Creates a BodyAssertionOperator object given its JSON representation.
     *
     * @param operator JSON representation of the operator.
     */
    public BodyAssertionOperator(JSONObject operator) {
        this.id = (int) ((long) operator.get("id"));
        this.operatorId = (int) ((long) operator.get("operatorId"));

        // get operator input
        if (operator.containsKey("input")) {
            JSONObject input = (JSONObject) operator.get("input");
            this.inputType = (int) ((long) input.get("id"));
            if (input.containsKey("value")) {
                this.inputValue = (String) input.get("value");
            }
        }

        // check if operator has a following operator
        if (operator.containsKey("followedBy")) {
            this.followedByOperator = new BodyAssertionOperator((JSONObject) operator.get("followedBy"));
        }
    }

    public BodyAssertionOperator(int id, int operatorId, int modelId, int inputType, String inputValue, BodyAssertionOperator followedByOperator) {
        this.id = id;
        this.operatorId = operatorId;
        this.modelId = modelId;
        this.inputType = inputType;
        this.inputValue = inputValue;
        this.followedByOperator = followedByOperator;
    }

    public BodyAssertionOperator(int operatorId, int inputType, String inputValue, BodyAssertionOperator followedByOperator) {
        this(0, operatorId, -1, inputType, inputValue, followedByOperator);
    }

    public BodyAssertionOperator(int operatorId, int inputType, BodyAssertionOperator followedByOperator) {
        this(0, operatorId, -1, inputType, "", followedByOperator);
    }

    public BodyAssertionOperator(int operatorId, int inputType) {
        this(operatorId, inputType, null);
    }

    /**
     * Loads the operator with the given id from the database.
     *
     * @param connection
     * @param id         Id of the operator to load.
     * @param modelId    Id of the model, that the operator belongs to.
     * @return BodyAssertionOperator object
     * @throws SQLException
     */
    public static BodyAssertionOperator loadFromDatabase(Connection connection, int id, int modelId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM BodyAssertionOperator WHERE id=? AND modelId=?;");
        statement.setInt(1, id);
        statement.setInt(2, modelId);

        // execute query
        ResultSet queryResult = statement.executeQuery();

        // check for results
        if (queryResult.next()) {
            int operatorId = queryResult.getInt("operatorId");
            int inputType = queryResult.getInt("inputType");
            String inputValue = queryResult.getString("inputValue");
            int followedById = queryResult.getInt("followedBy");
            BodyAssertionOperator followedByOperator = null;
            if (!queryResult.wasNull()) {
                followedByOperator = BodyAssertionOperator.loadFromDatabase(connection, followedById, modelId);
            }

            statement.close();

            return new BodyAssertionOperator(id, operatorId, modelId, inputType, inputValue, followedByOperator);
        } else {
            statement.close();

            // there does not exist an operator with the given id in the database
            throw new AssertionOperatorNotFoundException(id);
        }
    }

    /**
     * Stores the current operator (and its following operator, if there exists one) to the database.
     *
     * @param connection
     * @param modelId    Id of the model, that the operator belongs to.
     * @throws SQLException If storing the operator failed.
     */
    public void persist(Connection connection, int modelId) throws SQLException {
        this.modelId = modelId;

        String statementStr = "INSERT INTO BodyAssertionOperator (id, operatorId, modelId, inputType, inputValue) VALUES (?,?,?,?,?);";
        if (this.hasFollowingOperator()) {
            statementStr = "INSERT INTO BodyAssertionOperator (id, operatorId, modelId, inputType, inputValue, followedBy) VALUES (?,?,?,?,?,?);";
        }

        PreparedStatement statement = connection.prepareStatement(statementStr);
        statement.setInt(1, this.id);
        statement.setInt(2, this.operatorId);
        statement.setInt(3, this.modelId);
        statement.setInt(4, this.inputType);
        statement.setString(5, this.inputValue);
        if (this.hasFollowingOperator()) {
            statement.setInt(6, this.followedByOperator.getId());
        }

        statement.executeUpdate();
        statement.close();

        // persist following operator if there exists one
        if (this.hasFollowingOperator()) {
            this.followedByOperator.persist(connection, modelId);
        }
    }

    public JSONObject toJSONObject() {
        JSONObject operator = new JSONObject();

        operator.put("id", this.id);
        operator.put("operatorId", this.operatorId);
        JSONObject inputJSON = new JSONObject();
        inputJSON.put("id", this.inputType);
        inputJSON.put("value", this.inputValue);
        operator.put("input", inputJSON);

        if (this.hasFollowingOperator()) {
            operator.put("followedBy", this.followedByOperator.toJSONObject());
        }

        return operator;
    }

    public String toString() {
        String description = "";
        if (this.operatorId == ResponseBodyOperator.HAS_TYPE.getId()) {
            OperatorInput input = OperatorInput.fromId(this.inputType);
            if (input != null) {
                description = "has type " + input.getValue();
            } else {
                // no predefined type but a schema defined in metadata editor
                description = "has type " + this.inputValue;
            }
        } else if (this.operatorId == ResponseBodyOperator.HAS_FIELD.getId()) {
            description = "has field \"" + this.inputValue + "\"";
        } else if (this.operatorId == ResponseBodyOperator.HAS_LIST_ENTRY_THAT.getId()) {
            description = "has list entry that";
        } else if (this.operatorId == ResponseBodyOperator.ALL_LIST_ENTRIES.getId()) {
            description = "all list entries";
        }
        if (this.hasFollowingOperator()) {
            description += " " + this.getFollowingOperator().toString();
        }
        return description;
    }

    /**
     * Whether the current operator is followed by another operator.
     *
     * @return Whether the current operator is followed by another operator.
     */
    public boolean hasFollowingOperator() {
        return this.followedByOperator != null;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOperatorId() {
        return this.operatorId;
    }

    public BodyAssertionOperator getFollowingOperator() {
        return this.followedByOperator;
    }

    public int getInputType() {
        return this.inputType;
    }

    public String getInputValue() {
        return this.inputValue;
    }

    public void setFollowedByOperator(BodyAssertionOperator followedByOperator) {
        this.followedByOperator = followedByOperator;
    }
}
