package mm.expenses.manager.common.postgresql.exception;

/**
 * Specific exception to be thrown in case of any issue with {@link mm.expenses.manager.common.postgresql.specification.Operation}.
 */
public class OperationException extends RuntimeException {

    public static final String UNRECOGNIZABLE_OPERATION_MESSAGE = "Unrecognizable operation: %s";

    public OperationException(final String message) {
        super(message);
    }

    public OperationException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
