package mm.expenses.manager.exception.api;

import mm.expenses.manager.exception.ExceptionType;
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
