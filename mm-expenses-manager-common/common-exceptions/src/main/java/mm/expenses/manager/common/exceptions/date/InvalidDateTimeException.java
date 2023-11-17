package mm.expenses.manager.common.exceptions.date;

import mm.expenses.manager.common.exceptions.base.EmUncheckedException;
import mm.expenses.manager.common.exceptions.base.ExceptionType;

/**
 * Exception class for issues regarding date and/or dtime.
 */
public class InvalidDateTimeException extends EmUncheckedException {

    public InvalidDateTimeException(final ExceptionType exceptionType) {
        super(exceptionType);
    }

    public InvalidDateTimeException(final ExceptionType exceptionType, final Throwable exception) {
        super(exceptionType, exception);
    }

}
