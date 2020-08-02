package mm.expenses.manager.order.order.exception;

import lombok.Getter;
import mm.expenses.manager.validator.ValidationException;

@Getter
public class OrderValidationException extends ValidationException {

    public OrderValidationException(final String message) {
        super(message);
    }

}
