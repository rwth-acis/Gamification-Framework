package i5.las2peer.apiTestModel.exception;

import java.sql.SQLException;

public class TestModelNotFoundException extends SQLException {

    public TestModelNotFoundException(int id) {
        super("Test model with id " + id + " could not be found.");
    }
}
