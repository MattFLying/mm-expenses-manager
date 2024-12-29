package mm.expenses.manager.common.utils.exception;

/**
 * Exception thrown when more than one price with the same currency code is used.
 */
public class PriceIllegalArgumentException extends IllegalArgumentException {

    public static final String ERROR_MESSAGE = "Multiple prices with the same currency are not allowed";

    public PriceIllegalArgumentException() {
        super(ERROR_MESSAGE);
    }

}
