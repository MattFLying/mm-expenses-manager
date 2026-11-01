package mm.expenses.manager.keycloak.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.keycloak.model.UserResponse;
import mm.expenses.manager.keycloak.request.UserRequestSender;
import mm.expenses.manager.keycloak.model.UserRegisteredModel;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.keycloak.events.Event;
import org.keycloak.models.UserModel;

import java.util.Objects;
import java.util.UUID;

/**
 * Sends Keycloak events mapped to expected Kafka models to be processed from specific topics.
 */
@Slf4j
public class UserKafkaSender extends KafkaEventSender implements UserRequestSender {

    static final String USER_REGISTER_TOPIC = "kc-spi-user-register";

    public UserKafkaSender(final KafkaProducer<String, String> producer, final ObjectMapper mapper) {
        super(producer, mapper);
    }

    /**
     * Sends user's register message via kafka to process user creation in the system based on
     * already registered user directly on Keycloak side
     */
    @Override
    public void registerUser(final Event event, final UserModel user) {
        try {
            producer.send(
                    record(USER_REGISTER_TOPIC, event.getUserId(), new UserRegisteredModel(event, user)),
                    (metadata, exception) -> {
                        if (Objects.nonNull(exception)) {
                            log.error("[KEYCLOAK-SPI] User registration failed to send event. EventID: {}, ErrorMessage: {}", event.getId(), exception.getMessage(), exception);
                        } else {
                            log.info("[KEYCLOAK-SPI] User registration event has been sent successfully. EventID: {}", event.getId());
                        }
                    }
            );
        } catch (final JsonProcessingException exception) {
            log.error("[KEYCLOAK-SPI] Cannot map registered user's data to send event via Kafka. EventID: {}, ErrorMessage: {}", event.getId(), exception.getMessage(), exception);
        } catch (final Exception exception) {
            log.error("[KEYCLOAK-SPI] Unknown error occurred during sending user registration event via Kafka. EventID: {}, ErrorMessage: {}", event.getId(), exception.getMessage(), exception);
        }
    }

    /**
     * Not available for Kafka definition in this case and returns null.
     */
    @Override
    public UserResponse getUser(final UUID id) {
        return null;
    }


    /**
     * Highest priority for Kafka handling, so it must be the default '0' value as defined by default.
     */
    @Override
    public int priority() {
        return super.priority();
    }

}
