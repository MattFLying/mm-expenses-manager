package mm.expenses.manager.order.order.exception;

public class OrderCreationException extends RuntimeException {

    public OrderCreationException(final String message) {
        super(message);
    }

    public OrderCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
