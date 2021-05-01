package mm.expenses.manager.finance.exception;

import mm.expenses.manager.exception.EmCheckedException;
import mm.expenses.manager.exception.ExceptionType;
import mm.expenses.manager.exception.api.feign.ApiFeignClientException;
import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.Optional;

/**
 * Exception thrown when something went wrong with external/internal currency providers.
 */
public class CurrencyProviderException extends EmCheckedException {

    private final HttpStatus status;
    private final String clientMessage;

    public CurrencyProviderException(final ExceptionType exceptionMessage) {
        super(exceptionMessage);
        this.status = null;
        this.clientMessage = null;
    }

    public CurrencyProviderException(final ExceptionType exceptionMessage, final Throwable exception) {
        super(exceptionMessage, exception);
        this.status = null;
        this.clientMessage = null;
    }

    public CurrencyProviderException(final ExceptionType exceptionMessage, final ApiFeignClientException clientException) {
        super(exceptionMessage, clientException);
        this.status = clientException.getStatus();
        this.clientMessage = clientException.getMessage();
    }

    /**
     * Checks if exception has been thrown because of failed external provider
     */
    public boolean isHttpError() {
        return Objects.nonNull(status);
    }

    public Optional<HttpStatus> getClientStatus() {
        return Optional.ofNullable(status);
    }

    public Optional<String> getClientMessage() {
        return Optional.ofNullable(clientMessage);
    }

}
