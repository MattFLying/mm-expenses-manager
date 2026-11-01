package mm.expenses.manager.keycloak.exception;

import mm.expenses.manager.common.exceptions.base.EmUncheckedException;

import java.time.Instant;

/**
 * Simple exception to be thrown during Keycloak SPI operations.
 */
public class KeycloakSpiException extends EmUncheckedException {

    private final Instant occurred;

    public KeycloakSpiException(final KeycloakSpiExceptionMessage exceptionType) {
        super(exceptionType);
        this.occurred = timeNow();
    }

    public KeycloakSpiException(final KeycloakSpiExceptionMessage exceptionType, final Throwable cause) {
        super(exceptionType, cause);
        this.occurred = timeNow();
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
