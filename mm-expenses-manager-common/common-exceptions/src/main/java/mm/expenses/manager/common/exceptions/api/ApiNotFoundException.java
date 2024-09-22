package mm.expenses.manager.common.exceptions.api;

import mm.expenses.manager.common.exceptions.base.ExceptionType;

public class ApiNotFoundException extends ApiException {

    public ApiNotFoundException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    @Override
    public int httpStatus() {
        return 404;
    }

}
