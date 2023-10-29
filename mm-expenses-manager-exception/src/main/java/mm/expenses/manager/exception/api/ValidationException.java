package mm.expenses.manager.exception.api;

import mm.expenses.manager.exception.ExceptionType;
import org.springframework.http.HttpStatus;

import java.util.Objects;

public class ValidationException extends ApiException {

    private final RuntimeException validationCause;

    public ValidationException(final ExceptionType exceptionType) {
        super(exceptionType);
        this.validationCause = null;
    }

    public ValidationException(final ExceptionType exceptionType, final jakarta.validation.ValidationException validationException) {
        super(exceptionType, validationException);
        this.validationCause = validationException;
    }

    public RuntimeException getValidationCause() {
        if (Objects.isNull(validationCause)) {
            return null;
        }
        return validationCause;
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
