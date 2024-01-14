package mm.expenses.manager.common.beans.exception;

import mm.expenses.manager.common.exceptions.base.EmUncheckedException;
import mm.expenses.manager.common.exceptions.base.ExceptionType;

/**
 * Exception using for any asynchronous operations.
 */
public class AsyncException extends EmUncheckedException {

    public AsyncException(final ExceptionType exceptionMessage) {
        super(exceptionMessage);
    }

}
