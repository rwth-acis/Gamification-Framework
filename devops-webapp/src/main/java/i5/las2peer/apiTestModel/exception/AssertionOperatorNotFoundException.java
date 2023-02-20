package i5.las2peer.apiTestModel.exception;

import java.sql.SQLException;

public class AssertionOperatorNotFoundException extends SQLException {

    public AssertionOperatorNotFoundException(int id) {
        super("Operator with id " + id + " could not be found.");
    }
}
