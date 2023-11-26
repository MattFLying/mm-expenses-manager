package mm.expenses.manager.common.web.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import mm.expenses.manager.common.exceptions.base.EmAppException;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.Instant;

@Schema(name = "ExceptionMessage", description = "Exception message response.")
public record ExceptionMessage(@Schema(description = "Exception code.") String code,
                               @Schema(description = "Exception status.") String status,
                               @Schema(description = "Exception message.") String message,
                               @Schema(description = "Exception date when occurred.") Instant occurredAt) implements Serializable {

    private static final HttpStatus INTERNAL_SERVER_ERROR_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
    private static final String INTERNAL_SERVER_ERROR_CODE = "internal-server-error";

    public static ExceptionMessage fromApiException(final ApiException apiException) {
        final var status = apiException.httpStatus();
        final var formattedStatus = formatStatus(status);
        return new ExceptionMessage(apiException.getType().getCode(), formattedStatus, apiException.getMessage(), apiException.occurredAt());
    }

    public static ExceptionMessage fromCustomException(final EmAppException exception) {
        final var formattedStatus = formatStatus(INTERNAL_SERVER_ERROR_STATUS);
        return new ExceptionMessage(exception.getType().getCode(), formattedStatus, exception.getMessage(), Instant.now());
    }

    public static ExceptionMessage fromException(final Exception exception) {
        final var formattedStatus = formatStatus(INTERNAL_SERVER_ERROR_STATUS);
        return new ExceptionMessage(INTERNAL_SERVER_ERROR_CODE, formattedStatus, exception.getMessage(), Instant.now());
    }

    public static ExceptionMessage of(final String message, final HttpStatus status) {
        final var formattedStatus = formatStatus(status);
        return new ExceptionMessage(status.getReasonPhrase(), formattedStatus, message, Instant.now());
    }

    public static ExceptionMessage of(final String code, final String message, final HttpStatus status) {
        final var formattedStatus = formatStatus(status);
        return new ExceptionMessage(code, formattedStatus, message, Instant.now());
    }

    public static String formatStatus(final HttpStatus status) {
        return String.format("%d(%s)", status.value(), status.getReasonPhrase());
    }

}
