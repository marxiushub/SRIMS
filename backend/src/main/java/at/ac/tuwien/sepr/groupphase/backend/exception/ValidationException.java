package at.ac.tuwien.sepr.groupphase.backend.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ValidationException extends RuntimeException {

    private final List<LocalizedError> errors;
    private final String deMessage;

    public ValidationException(String message, String deMessage, List<LocalizedError> errors) {
        super(buildDetailedMessage(message, errors));
        this.errors = errors;
        this.deMessage = deMessage;
    }

    public ValidationException(String message, String deMessage) {
        super(message);
        this.errors = new ArrayList<>();
        this.deMessage = deMessage;
    }

    private static String buildDetailedMessage(String baseMessage, List<LocalizedError> errors) {
        if (errors == null || errors.isEmpty()) {
            return baseMessage;
        }

        String joinedErrors = errors.stream()
            .map(LocalizedError::message)
            .collect(Collectors.joining(", "));

        return baseMessage + ": " + joinedErrors;
    }

    public List<LocalizedError> getErrors() {
        return errors;
    }

    public String getDeMessage() {
        return deMessage;
    }
}
