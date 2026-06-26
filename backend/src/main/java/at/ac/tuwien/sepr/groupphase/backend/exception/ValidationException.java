package at.ac.tuwien.sepr.groupphase.backend.exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<String> errors;
    private final String deMessage;

    public ValidationException(String message, String deMessage, List<String> errors) {
        super(message + ": " + String.join(", ", errors));
        this.errors = errors;
        this.deMessage = deMessage;
    }

    public ValidationException(String message, String deMessage) {
        super(message);
        this.errors = new ArrayList<>();
        this.deMessage = deMessage;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getDeMessage() {
        return deMessage;
    }
}
