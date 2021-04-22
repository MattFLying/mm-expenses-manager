package mm.expenses.manager.exception;

import java.util.Objects;

/**
 * Base checked exception.
 */
public class EmCheckedException extends Exception implements EmAppException {

    private final ExceptionType exceptionType;

    public EmCheckedException(final ExceptionType exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
    }

    public EmCheckedException(final ExceptionType exceptionType, final Throwable cause) {
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
