package mm.expenses.manager.common.beans.exception.api;

import mm.expenses.manager.common.exceptions.base.ExceptionType;
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
