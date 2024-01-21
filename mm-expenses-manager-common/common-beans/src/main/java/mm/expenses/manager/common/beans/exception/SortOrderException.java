package mm.expenses.manager.common.beans.exception;

import mm.expenses.manager.common.exceptions.base.EmUncheckedException;
import mm.expenses.manager.common.exceptions.base.ExceptionType;

/**
 * Exception using for any sorting orders issues.
 */
public class SortOrderException extends EmUncheckedException {

    public SortOrderException(final ExceptionType exceptionMessage) {
        super(exceptionMessage);
    }

    public SortOrderException(final ExceptionType exceptionMessage, final Throwable exception) {
        super(exceptionMessage, exception);
    }

}
