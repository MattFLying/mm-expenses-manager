package mm.expenses.manager.common.exceptions.api;

import mm.expenses.manager.common.exceptions.base.ExceptionType;

public class ApiInternalErrorException extends ApiException {

    public ApiInternalErrorException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    @Override
    public int httpStatus() {
        return 500;
    }

}
