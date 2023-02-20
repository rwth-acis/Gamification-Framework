package i5.las2peer.apiTestModel.exception;

import java.sql.SQLException;

public class TestRequestNotFoundException extends SQLException {

    public TestRequestNotFoundException(int testRequestId) {
        super("Test request with id " + testRequestId + " could not be found.");
    }
}
