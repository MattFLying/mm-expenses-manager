package mm.expenses.manager.common.exceptions.api;

import mm.expenses.manager.common.exceptions.base.ExceptionType;

public class ApiMethodNotAllowed extends ApiException {

    public ApiMethodNotAllowed(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    public ApiMethodNotAllowed(final ExceptionType exceptionType, final Throwable cause) {
        super(exceptionType, cause);
    }

    @Override
    public int httpStatus() {
        return 405;
    }

}
