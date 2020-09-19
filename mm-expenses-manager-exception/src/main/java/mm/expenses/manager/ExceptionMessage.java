package mm.expenses.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class ExceptionMessage {

    private static final String INTERNAL_SERVER_ERROR_CODE = "internal-server-error";

    private final String code;
    private final String status;
    private final String message;
    private final Instant occurredAt;

    static ExceptionMessage fromApiException(final ApiException apiException) {
        final var status = apiException.httpStatus();
        final var formattedStatus = formatStatus(status);
        return new ExceptionMessage(apiException.code(), formattedStatus, apiException.getMessage(), apiException.occurredAt());
    }

    static ExceptionMessage fromException(final Exception exception) {
        final var status = HttpStatus.INTERNAL_SERVER_ERROR;
        final var formattedStatus = formatStatus(status);
        return new ExceptionMessage(INTERNAL_SERVER_ERROR_CODE, formattedStatus, exception.getMessage(), Instant.now());
    }

    public static String formatStatus(final HttpStatus status) {
        return String.format("%d(%s)", status.value(), status.getReasonPhrase());
    }

}
