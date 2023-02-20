package i5.las2peer.apiTestModel.exception;

import java.sql.SQLException;

public class AssertionNotFoundException extends SQLException {

    public AssertionNotFoundException(int assertionId) {
        super("Assertion with id " + assertionId + " could not be found.");
    }
}
