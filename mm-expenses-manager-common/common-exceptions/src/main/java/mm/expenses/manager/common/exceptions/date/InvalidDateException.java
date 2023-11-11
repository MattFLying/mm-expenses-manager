package mm.expenses.manager.common.exceptions.date;

import mm.expenses.manager.common.exceptions.base.EmUncheckedException;
import mm.expenses.manager.common.exceptions.base.ExceptionType;

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
