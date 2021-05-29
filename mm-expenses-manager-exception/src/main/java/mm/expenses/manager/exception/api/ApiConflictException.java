package mm.expenses.manager.exception.api;

import mm.expenses.manager.exception.ExceptionType;
import org.springframework.http.HttpStatus;

public class ApiConflictException extends ApiException {

    public ApiConflictException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.CONFLICT;
    }

}
