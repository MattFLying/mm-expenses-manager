package mm.expenses.manager.order.exception;

import mm.expenses.manager.common.exceptions.base.ExceptionType;
import mm.expenses.manager.common.web.exception.ApiBadRequestException;

/**
 * Validation exception for orders.
 */
public class OrderValidationException extends ApiBadRequestException {

    public OrderValidationException(final ExceptionType exceptionMessage) {
        super(exceptionMessage);
    }

}