package mm.expenses.manager.product.exception;

import mm.expenses.manager.exception.ExceptionType;
import mm.expenses.manager.exception.api.ApiBadRequestException;

/**
 * Validation exception for products.
 */
public class ProductValidationException extends ApiBadRequestException {

    public ProductValidationException(final ExceptionType exceptionMessage) {
        super(exceptionMessage);
    }

}
