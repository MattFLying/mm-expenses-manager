package mm.expenses.manager.product.exception;

import mm.expenses.manager.common.exceptions.base.ExceptionType;
import mm.expenses.manager.common.beans.exception.api.ApiBadRequestException;

/**
 * Validation exception for products.
 */
public class ProductValidationException extends ApiBadRequestException {

    public ProductValidationException(final ExceptionType exceptionMessage) {
        super(exceptionMessage);
    }

}
