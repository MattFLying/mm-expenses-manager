package mm.expenses.manager.exception.api;

import org.springframework.http.HttpStatus;

public class ApiBadRequestException extends ApiException {

    public ApiBadRequestException(final String code, final String message) {
        super(code, message);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
