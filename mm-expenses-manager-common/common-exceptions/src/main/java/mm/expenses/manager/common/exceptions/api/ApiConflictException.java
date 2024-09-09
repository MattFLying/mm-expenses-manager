package mm.expenses.manager.common.exceptions.api;

import mm.expenses.manager.common.exceptions.base.ExceptionType;

public class ApiConflictException extends ApiException {

    public ApiConflictException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    @Override
    public int httpStatus() {
        return 409;
    }

}
