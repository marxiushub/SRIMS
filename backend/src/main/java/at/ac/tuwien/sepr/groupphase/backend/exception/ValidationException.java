package at.ac.tuwien.sepr.groupphase.backend.exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(String message, List<String> errors) {
        super(message + ": " + String.join(", ", errors));
        this.errors = errors;
    }

    public ValidationException(String message) {
        super(message);
        this.errors = new ArrayList<>();
    }

    public List<String> getErrors() {
        return errors;
    }
}
