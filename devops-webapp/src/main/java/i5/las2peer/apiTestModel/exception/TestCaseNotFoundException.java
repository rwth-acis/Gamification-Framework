package i5.las2peer.apiTestModel.exception;

import java.sql.SQLException;

public class TestCaseNotFoundException extends SQLException {

    public TestCaseNotFoundException(int testCaseId) {
        super("Test case with id " + testCaseId + " could not be found.");
    }
}
