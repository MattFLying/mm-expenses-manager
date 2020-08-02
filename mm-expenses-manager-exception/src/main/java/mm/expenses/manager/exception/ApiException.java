package mm.expenses.manager.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public abstract class ApiException extends RuntimeException {

    private final String code;
    private final Instant occurred;

    public ApiException(final String code, final String message) {
        super(message);
        this.code = code;
        this.occurred = timeNow();
    }

    public ApiException(final String code, final String message, final Throwable cause) {
        super(message, cause);
        this.code = code;
        this.occurred = timeNow();
    }

    public abstract HttpStatus httpStatus();

    public String code() {
        return code;
    }

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
