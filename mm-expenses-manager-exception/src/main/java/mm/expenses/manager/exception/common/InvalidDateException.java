package mm.expenses.manager.exception.common;

import mm.expenses.manager.exception.EmUncheckedException;
import mm.expenses.manager.exception.ExceptionType;

/**
 * Exception class for issues regarding to date.
 */
public class InvalidDateException extends EmUncheckedException {

    public InvalidDateException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    public InvalidDateException(final ExceptionType exceptionType, final Throwable exception) {
        super(exceptionType, exception);
    }

}
