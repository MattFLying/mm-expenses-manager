package mm.expenses.manager.order.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.kafka.consumer.AsyncKafkaConsumer;
import mm.expenses.manager.order.async.message.ProductManagementConsumerMessage;
import mm.expenses.manager.order.product.ProductService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("'${app.configuration.message-broker.type}' == 'kafka' and ${app.configuration.message-broker.enabled}")
public class OrderKafkaConsumer extends AsyncKafkaConsumer {

    private final ProductService productService;

    @Bean
    Consumer<ProductManagementConsumerMessage> productManagement() {
        return message -> {
            logReceivedMessage(message);

            final var binding = message.getConsumerBindingName();
            final var topic = message.getConsumerTopicName();
            final var operation = getOperationOrUndefined(message.getOperation());
            switch (operation) {
                case CREATE -> productService.createProductFromKafkaTopic(message);
                case UPDATE -> productService.updateProductFromKafkaTopic(message);
                case DELETE -> productService.deleteProductFromKafkaTopic(message);
                case UNDEFINED -> logUndefinedOperation(binding, topic, message);
                default -> logUnknownOperation(binding, topic, message);
            }
        };
    }

}
