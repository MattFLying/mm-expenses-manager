package mm.expenses.manager.common.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.async.AsyncBinding;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.async.AsyncMessageConsumer;

/**
 * Basic implementation of {@link AsyncMessageConsumer} for Kafka consumer.
 * Should be extended by specific consumers in specific modules that needs to consume Kafka events in functional style.
 */
@Slf4j
public abstract class AsyncKafkaConsumer implements AsyncMessageConsumer {

    /**
     * Maps async operation {@link AsyncKafkaOperation} and retrieves original value or UNDEFINED.
     *
     * @param operation - original message's async operation
     * @return original value or UNDEFINED
     */
    protected AsyncKafkaOperation getOperationOrUndefined(final AsyncKafkaOperation operation) {
        return AsyncKafkaOperation.of(operation);
    }

    /**
     * Logs message {@link AsyncBinding} of received kafka event.
     *
     * @param message - kafka consumer message
     */
    protected void logReceivedMessage(final AsyncBinding message) {
        log.info("{}", logMessage(message));
    }

    /**
     * Logs message for undefined async operation {@link AsyncKafkaOperation}.
     *
     * @param binding - kafka consumer message binding name
     * @param topic   - kafka consumer message topic name
     * @param message - kafka consumer message
     */
    protected void logUndefinedOperation(final String binding, final String topic, final AsyncBinding message) {
        log.warn("Operation for binding: {} and topic: {} is not defined. Message: {}", binding, topic, message);
    }

    /**
     * Logs message for unknown async operation {@link AsyncKafkaOperation} that cannot be handled in consumer.
     *
     * @param binding - kafka consumer message binding name
     * @param topic   - kafka consumer message topic name
     * @param message - kafka consumer message
     */
    protected void logUnknownOperation(final String binding, final String topic, final AsyncBinding message) {
        log.debug("Unknown operation for binding: {} and topic: {}. Message: {}", binding, topic, message);
    }

}
