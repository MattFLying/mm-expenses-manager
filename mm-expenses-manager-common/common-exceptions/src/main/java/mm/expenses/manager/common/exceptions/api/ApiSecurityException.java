package mm.expenses.manager.common.exceptions.api;

import mm.expenses.manager.common.exceptions.base.ExceptionType;

/**
 * Basic representation of security exceptions to be handled by APIs.
 */
public abstract class ApiSecurityException extends ApiException {

    protected ApiSecurityException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    protected ApiSecurityException(final ExceptionType exceptionType, final Throwable cause) {
        super(exceptionType, cause);
    }

}
