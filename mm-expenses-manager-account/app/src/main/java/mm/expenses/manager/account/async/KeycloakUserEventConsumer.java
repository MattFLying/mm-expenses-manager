package mm.expenses.manager.account.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.kafka.consumer.AsyncKafkaConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("'${app.configuration.message-broker.type}' == 'kafka' and ${app.configuration.message-broker.enabled}")
class KeycloakUserEventConsumer extends AsyncKafkaConsumer {

    private final UserMessageMapper userMessageMapper;
    private final AsyncUserService userService;

    @Bean
    Consumer<String> keycloakSpiUserRegistration() {
        return message -> {
            log.info("Received kafka message from Keycloak with new user registered= {}", message);

            final var mapped = userMessageMapper.registration(message);
            final var result = userService.createFromEvent(mapped);

            log.info("Newly registered user in Keycloak successfully created in database={}", result);
        };
    }

}
