package mm.expenses.manager.exception;

public class InvalidDateException extends RuntimeException {

    public InvalidDateException(final String message) {
        super(message);
    }

    public InvalidDateException(final String message, final Throwable exception) {
        super(message, exception);
    }

}
