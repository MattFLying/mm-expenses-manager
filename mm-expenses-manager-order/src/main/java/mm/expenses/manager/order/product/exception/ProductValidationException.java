package mm.expenses.manager.order.product.exception;

import lombok.Getter;
import mm.expenses.manager.order.validator.ValidationException;

@Getter
public class ProductValidationException extends ValidationException {

    public ProductValidationException(final String message) {
        super(message);
    }

}
