package mm.expenses.manager.keycloak.request;

/**
 * Interface to send requests from Keycloak SPI side with different specific implementation.
 */
public interface RequestSender {

    /**
     * @return true if sender is available to be used, otherwise false.
     */
    boolean isAvailable();

    /**
     * Priority of the request sender that can be configured if needed. By default returns 0 which means the highest priority;
     */
    default int priority() {
        return 0;
    }

}
