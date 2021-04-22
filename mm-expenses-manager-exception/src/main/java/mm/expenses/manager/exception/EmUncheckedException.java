package mm.expenses.manager.exception;

import java.util.Objects;

/**
 * Base unchecked exception.
 */
public class EmUncheckedException extends RuntimeException implements EmAppException {

    private final ExceptionType exceptionType;

    public EmUncheckedException(final ExceptionType exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
    }

    public EmUncheckedException(final ExceptionType exceptionType, final Throwable cause) {
        super(exceptionType.getMessage(), cause);
        this.exceptionType = exceptionType;
    }

    @Override
    public String getMessage() {
        if (Objects.isNull(getType()) || (Objects.nonNull(getType()) && Objects.isNull(getType().getMessage()))) {
            return super.getMessage();
        }
        return getType().getMessage();
    }

    @Override
    public ExceptionType getType() {
        return exceptionType;
    }

}
