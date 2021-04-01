package mm.expenses.manager.exception.api;

import org.springframework.http.HttpStatus;

public class ApiNotFoundException extends ApiException {

    public ApiNotFoundException(final String code, final String message) {
        super(code, message);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.NOT_FOUND;
    }

}
