package mm.expenses.manager.order.validator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidationException extends RuntimeException {

    public ValidationException(final String message) {
        super(message);
        log.error("Validation error occurred with message: {}", message);
    }

    public ValidationException(final String message, final Throwable cause) {
        super(message, cause);
        log.error("Validation error occurred with message: {}", message, cause);
    }

}
