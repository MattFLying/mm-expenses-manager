package mm.expenses.manager.common.web.exception;

import mm.expenses.manager.common.exceptions.base.ExceptionType;
import org.springframework.http.HttpStatus;

public class ApiNotFoundException extends ApiException {

    public ApiNotFoundException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.NOT_FOUND;
    }

}
