package mm.expenses.manager.exception.api;

import mm.expenses.manager.exception.ExceptionType;
import org.springframework.http.HttpStatus;

public class ApiInternalErrorException extends ApiException {

    public ApiInternalErrorException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
