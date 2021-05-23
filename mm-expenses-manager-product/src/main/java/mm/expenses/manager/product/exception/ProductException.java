package mm.expenses.manager.product.exception;

import mm.expenses.manager.exception.EmUncheckedException;
import mm.expenses.manager.exception.ExceptionType;

/**
 * Default exception for any cases of products.
 */
public class ProductException extends EmUncheckedException {

    public ProductException(final ExceptionType exceptionMessage, final Throwable exception) {
        super(exceptionMessage, exception);
    }

}
