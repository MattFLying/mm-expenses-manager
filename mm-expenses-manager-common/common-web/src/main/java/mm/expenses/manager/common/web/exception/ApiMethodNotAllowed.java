package mm.expenses.manager.common.web.exception;

import mm.expenses.manager.common.exceptions.base.ExceptionType;
import org.springframework.http.HttpStatus;

public class ApiMethodNotAllowed extends ApiException {

    public ApiMethodNotAllowed(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    public ApiMethodNotAllowed(final ExceptionType exceptionType, final Throwable cause) {
        super(exceptionType, cause);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.METHOD_NOT_ALLOWED;
    }

}
