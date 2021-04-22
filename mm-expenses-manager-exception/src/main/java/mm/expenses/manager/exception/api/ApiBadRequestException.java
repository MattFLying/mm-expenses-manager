package mm.expenses.manager.exception.api;

import mm.expenses.manager.exception.ExceptionType;
import org.springframework.http.HttpStatus;

public class ApiBadRequestException extends ApiException {

    public ApiBadRequestException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
