package mm.expenses.manager.exception;

import org.springframework.http.HttpStatus;

public class ApiInternalErrorException extends ApiException {

    public ApiInternalErrorException(final String code, final String message) {
        super(code, message);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
