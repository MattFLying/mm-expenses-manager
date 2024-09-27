package mm.expenses.manager.common.async;

import mm.expenses.manager.common.async.exception.AsyncExceptionMessage;
import mm.expenses.manager.common.exceptions.async.AsyncException;

import java.util.Objects;

/**
 * Interface for asynchronous consumer implementation to receive message on expected brokers.
 */
public interface AsyncMessageConsumer {

    /**
     * Prepares received message details as simple string to be logged.
     *
     * @param message - message received on specific topic
     * @param <T>     - specific message implementation of {@link AsyncConsumerBinding}
     * @return received message details
     */
    default <T extends AsyncConsumerBinding> String logMessage(final T message) {
        if (Objects.isNull(message)) {
            throw new AsyncException(AsyncExceptionMessage.ASYNC_CONSUMER_MESSAGE_IS_NULL);
        }
        final var classType = message.getClass();
        final var binding = message.consumerBindingName();
        final var topic = message.consumerTopicName();
        if (Objects.isNull(binding) || Objects.isNull(topic)) {
            throw new AsyncException(AsyncExceptionMessage.ASYNC_CONSUMER_BINDING_OR_TOPIC_IS_NULL.withParameters(binding, topic));
        }
        return String.format("Received message on binding: %s, topic: %s, mapped to: %s. Message body: %s", binding, topic, classType, message);
    }

}
