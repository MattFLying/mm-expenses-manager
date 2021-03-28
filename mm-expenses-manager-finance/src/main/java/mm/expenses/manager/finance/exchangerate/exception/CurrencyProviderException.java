package mm.expenses.manager.finance.exchangerate.exception;

import mm.expenses.manager.exception.ApiFeignClientException;
import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.Optional;

public class CurrencyProviderException extends Exception {

    private final HttpStatus status;
    private final String clientMessage;

    public CurrencyProviderException(final String message, final ApiFeignClientException clientException) {
        super(message, clientException);
        this.status = clientException.getStatus();
        this.clientMessage = clientException.getMessage();
    }

    public CurrencyProviderException(final String message, final Throwable exception) {
        super(message, exception);
        this.status = null;
        this.clientMessage = null;
    }

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
