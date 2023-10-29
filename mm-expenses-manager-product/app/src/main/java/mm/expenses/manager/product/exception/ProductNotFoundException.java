package mm.expenses.manager.product.exception;

import mm.expenses.manager.exception.ExceptionType;
import mm.expenses.manager.exception.api.ApiNotFoundException;

/**
 * Validation exception for not found products.
 */
public class ProductNotFoundException extends ApiNotFoundException {

    public ProductNotFoundException(final ExceptionType exceptionMessage) {
        super(exceptionMessage);
    }

}
