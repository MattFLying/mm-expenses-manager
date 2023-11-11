package mm.expenses.manager.common.beans.exception.api;

import mm.expenses.manager.common.exceptions.base.EmUncheckedException;
import mm.expenses.manager.common.exceptions.base.ExceptionType;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public abstract class ApiException extends EmUncheckedException {

    private final Instant occurred;

    protected ApiException(final ExceptionType exceptionType) {
        super(exceptionType);
        this.occurred = timeNow();
    }

    protected ApiException(final ExceptionType exceptionType, final Throwable cause) {
        super(exceptionType, cause);
        this.occurred = timeNow();
    }

    public abstract HttpStatus httpStatus();

    /**
     * UTC
     */
    public Instant occurredAt() {
        return occurred;
    }

    private Instant timeNow() {
        return Instant.now();
    }

}
