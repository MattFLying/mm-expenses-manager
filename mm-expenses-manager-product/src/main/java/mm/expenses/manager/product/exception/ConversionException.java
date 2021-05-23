package mm.expenses.manager.product.exception;

import mm.expenses.manager.exception.EmUncheckedException;
import mm.expenses.manager.exception.ExceptionType;

/**
 * Exception using for any conversion issue.
 */
public class ConversionException extends EmUncheckedException {

    public ConversionException(final ExceptionType exceptionMessage, final Throwable exception) {
        super(exceptionMessage, exception);
    }

}
