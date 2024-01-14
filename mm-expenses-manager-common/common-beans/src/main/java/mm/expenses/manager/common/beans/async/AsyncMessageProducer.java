package mm.expenses.manager.common.beans.async;

/**
 * Interface for asynchronous producer implementation to send message via expected brokers.
 */
public interface AsyncMessageProducer {

    /**
     * Sends message asynchronously.
     *
     * @param message - message to be sent
     */
    void send(final AsyncProducerBinding message);

}
