package mm.expenses.manager.order.exception;

import mm.expenses.manager.common.exceptions.base.ExceptionType;
import mm.expenses.manager.common.web.exception.ApiNotFoundException;

/**
 * Validation exception for not found orders.
 */
public class OrderNotFoundException extends ApiNotFoundException {

    public OrderNotFoundException(final ExceptionType exceptionMessage) {
        super(exceptionMessage);
    }

}
