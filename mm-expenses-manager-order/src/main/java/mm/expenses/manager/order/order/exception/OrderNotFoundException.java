package mm.expenses.manager.order.order.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(final String message) {
        super(message);
    }

}
