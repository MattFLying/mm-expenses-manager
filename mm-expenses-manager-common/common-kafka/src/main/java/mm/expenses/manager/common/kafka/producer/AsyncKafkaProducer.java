package mm.expenses.manager.common.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.beans.async.AsyncMessageProducer;
import mm.expenses.manager.common.beans.async.AsyncProducerBinding;
import mm.expenses.manager.common.beans.exception.AsyncException;
import mm.expenses.manager.common.kafka.exception.KafkaExceptionMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Generic implementation of kafka producer {@link AsyncMessageProducer} to send event on specific topic.
 * Topic and binding is defined by message implementation {@link AsyncProducerBinding}.
 * There is no need for custom implementation as it is in functional approach that can be handled this way.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("'${app.configuration.message-broker.type}' == 'kafka' and ${app.configuration.message-broker.enabled}")
public class AsyncKafkaProducer implements AsyncMessageProducer {

    private final StreamBridge stream;

    @Override
    public void send(final AsyncProducerBinding message) {
        validateMessage(message);
        final var hasBeenSent = isMessageSent(message);
        logMessageSent(hasBeenSent, message);
    }

    private void logMessageSent(final boolean hasBeenSent, final AsyncProducerBinding message) {
        final var binding = message.getProducerBindingName();
        final var topic = message.getProducerTopicName();
        if (hasBeenSent) {
            log.info("Message sent to binding: {} on topic: {}. Body: {}", binding, topic, message);
        } else {
            throw new AsyncException(KafkaExceptionMessage.ASYNC_PRODUCER_MESSAGE_SEND_FAILED.withParameters(binding, topic, message));
        }
    }

    private boolean isMessageSent(final AsyncProducerBinding message) {
        return stream.send(message.getProducerBindingName(), message);
    }

    private void validateMessage(final AsyncProducerBinding message) {
        checkIfMessageIsNull(message);
        checkIfBindingOrTopicIsNullOrEmpty(message);
    }

    private void checkIfMessageIsNull(final AsyncProducerBinding message) {
        if (Objects.isNull(message)) {
            throw new AsyncException(KafkaExceptionMessage.ASYNC_PRODUCER_MESSAGE_IS_NULL);
        }
    }

    private void checkIfBindingOrTopicIsNullOrEmpty(final AsyncProducerBinding message) {
        if (StringUtils.isEmpty(message.getProducerBindingName()) || StringUtils.isEmpty(message.getProducerTopicName())) {
            throw new AsyncException(KafkaExceptionMessage.ASYNC_PRODUCER_BINDING_OR_TOPIC_IS_NULL);
        }
    }

}
