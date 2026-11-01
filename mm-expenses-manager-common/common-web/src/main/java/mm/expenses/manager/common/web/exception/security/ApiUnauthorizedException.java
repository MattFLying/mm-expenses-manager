package mm.expenses.manager.common.web.exception.security;

import mm.expenses.manager.common.exceptions.api.ApiSecurityException;
import mm.expenses.manager.common.exceptions.base.ExceptionType;
import org.springframework.http.HttpStatus;

public class ApiUnauthorizedException extends ApiSecurityException {

    public ApiUnauthorizedException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    public ApiUnauthorizedException(final ExceptionType exceptionType, final Throwable cause) {
        super(exceptionType, cause);
    }

    @Override
    public int httpStatus() {
        return HttpStatus.UNAUTHORIZED.value();
    }

}
