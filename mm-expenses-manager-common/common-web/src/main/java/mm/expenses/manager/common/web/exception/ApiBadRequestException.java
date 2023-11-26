package mm.expenses.manager.common.web.exception;

import mm.expenses.manager.common.exceptions.base.ExceptionType;
import org.springframework.http.HttpStatus;

public class ApiBadRequestException extends ApiException {

    public ApiBadRequestException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    public ApiBadRequestException(final ExceptionType exceptionType, final Throwable cause) {
        super(exceptionType, cause);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
