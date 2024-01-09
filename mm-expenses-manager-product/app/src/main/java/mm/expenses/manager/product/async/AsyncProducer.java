package mm.expenses.manager.product.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.configuration.kafka", name = "enabled", havingValue = "true")
class AsyncProducer implements AsyncMessageSender {

    private final StreamBridge stream;

    @Override
    public void send(final ProducerBinding message) {
        if (Objects.nonNull(message)) {
            if (!stream.send(message.binding(), message)) {
                log.error("Sending event to topic: {} failed. Body: {}", message.binding(), message);
            } else {
                log.info("Message sent to topic: {}. Body: {}", message.binding(), message);
            }
        } else {
            log.error("Cannot send message because the message is null.");
        }
    }

}
