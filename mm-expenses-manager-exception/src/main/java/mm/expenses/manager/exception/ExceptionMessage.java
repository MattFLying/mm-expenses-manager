package mm.expenses.manager.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.api.ApiException;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class ExceptionMessage implements Serializable {

    private static final HttpStatus INTERNAL_SERVER_ERROR_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
    private static final String INTERNAL_SERVER_ERROR_CODE = "internal-server-error";

    private final String code;
    private final String status;
    private final String message;
    private final Instant occurredAt;

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

    public static String formatStatus(final HttpStatus status) {
        return String.format("%d(%s)", status.value(), status.getReasonPhrase());
    }

}
