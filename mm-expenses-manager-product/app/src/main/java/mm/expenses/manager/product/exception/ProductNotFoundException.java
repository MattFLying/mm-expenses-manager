package mm.expenses.manager.product.exception;

import mm.expenses.manager.common.exceptions.base.ExceptionType;
import mm.expenses.manager.common.beans.exception.api.ApiNotFoundException;

/**
 * Validation exception for not found products.
 */
public class ProductNotFoundException extends ApiNotFoundException {

    public ProductNotFoundException(final ExceptionType exceptionMessage) {
        super(exceptionMessage);
    }

}
