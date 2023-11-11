package mm.expenses.manager.product.exception;

import mm.expenses.manager.common.exceptions.base.EmUncheckedException;
import mm.expenses.manager.common.exceptions.base.ExceptionType;

/**
 * Exception using for any conversion issue.
 */
public class ConversionException extends EmUncheckedException {

    public ConversionException(final ExceptionType exceptionMessage, final Throwable exception) {
        super(exceptionMessage, exception);
    }

}
