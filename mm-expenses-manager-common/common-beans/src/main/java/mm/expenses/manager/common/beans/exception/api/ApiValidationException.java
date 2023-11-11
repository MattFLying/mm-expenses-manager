package mm.expenses.manager.common.beans.exception.api;

import mm.expenses.manager.common.exceptions.base.ExceptionType;
import org.springframework.http.HttpStatus;

import java.util.Objects;

public class ApiValidationException extends ApiException {

    private final RuntimeException validationCause;

    public ApiValidationException(final ExceptionType exceptionType) {
        super(exceptionType);
        this.validationCause = null;
    }

    public ApiValidationException(final ExceptionType exceptionType, final jakarta.validation.ValidationException validationException) {
        super(exceptionType, validationException);
        this.validationCause = validationException;
    }

    public RuntimeException getValidationCause() {
        if (Objects.isNull(validationCause)) {
            return null;
        }
        return validationCause;
    }

    public boolean hasCause() {
        return Objects.nonNull(validationCause);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
