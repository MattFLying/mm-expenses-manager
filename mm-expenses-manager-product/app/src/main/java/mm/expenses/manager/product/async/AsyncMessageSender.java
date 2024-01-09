package mm.expenses.manager.product.async;

/**
 * Interface for asynchronous producer implementation to send message via expected brokers.
 */
public interface AsyncMessageSender {

    /**
     * Sends message.
     *
     * @param message - message
     */
    void send(final ProducerBinding message);

}
