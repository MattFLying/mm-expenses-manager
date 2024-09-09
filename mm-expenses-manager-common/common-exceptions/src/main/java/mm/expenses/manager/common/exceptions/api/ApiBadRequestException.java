package mm.expenses.manager.common.exceptions.api;

import mm.expenses.manager.common.exceptions.base.ExceptionType;

public class ApiBadRequestException extends ApiException {

    public ApiBadRequestException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    public ApiBadRequestException(final ExceptionType exceptionType, final Throwable cause) {
        super(exceptionType, cause);
    }

    @Override
    public int httpStatus() {
        return 400;
    }

}
