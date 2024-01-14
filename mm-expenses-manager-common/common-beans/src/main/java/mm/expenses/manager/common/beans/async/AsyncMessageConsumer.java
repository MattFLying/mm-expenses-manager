package mm.expenses.manager.common.beans.async;

import mm.expenses.manager.common.beans.exception.AsyncException;
import mm.expenses.manager.common.beans.exception.BeansExceptionMessage;

import java.util.Objects;

/**
 * Interface for asynchronous consumer implementation to receive message on expected brokers.
 */
public interface AsyncMessageConsumer {

    /**
     * Prepares received message details as simple string to be logged.
     *
     * @param consumerMessage - message received on specific topic
     * @param <T>             - specific message implementation of {@link AsyncConsumerBinding}
     * @return received message details
     */
    default <T extends AsyncConsumerBinding> String logMessage(final T consumerMessage) {
        if (Objects.isNull(consumerMessage)) {
            throw new AsyncException(BeansExceptionMessage.ASYNC_CONSUMER_MESSAGE_IS_NULL);
        }
        final var classType = consumerMessage.getClass();
        final var binding = consumerMessage.getConsumerBindingName();
        final var topic = consumerMessage.getConsumerTopicName();
        if (Objects.isNull(binding) || Objects.isNull(topic)) {
            throw new AsyncException(BeansExceptionMessage.ASYNC_CONSUMER_BINDING_OR_TOPIC_IS_NULL.withParameters(binding, topic));
        }
        return String.format("Received message on binding: %s, topic: %s, mapped to: %s. Message body: %s", binding, topic, classType, consumerMessage);
    }

}
