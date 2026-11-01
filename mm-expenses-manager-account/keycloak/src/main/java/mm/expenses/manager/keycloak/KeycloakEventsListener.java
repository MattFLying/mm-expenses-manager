package mm.expenses.manager.keycloak;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.keycloak.exception.KeycloakSpiException;
import mm.expenses.manager.keycloak.exception.KeycloakSpiExceptionMessage;
import mm.expenses.manager.keycloak.handler.UserEventHandler;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

import java.util.Objects;
import java.util.Optional;

/**
 * Listener to handle specific events from Keycloak to do some custom operations on specific events additionally
 * besides the Keycloak itself such as logging etc.
 */
@Slf4j
public class KeycloakEventsListener implements EventListenerProvider {

    private final KeycloakSession session;
    private final ObjectMapper mapper;
    private final UserEventHandler userEventHandler;

    public KeycloakEventsListener(final KeycloakSession keycloakSession, final KafkaProducer<String, String> kafkaProducer, final ObjectMapper objectMapper, final InternalProperties internalProperties) {
        this.session = keycloakSession;
        this.mapper = objectMapper;
        this.userEventHandler = new UserEventHandler(this.session, this.mapper, kafkaProducer, internalProperties);
    }

    @Override
    public void onEvent(final Event event) {
        try {
            if (Objects.nonNull(event)) {
                final var eventAsJson = mapper.writeValueAsString(event);

                Optional.ofNullable(event.getType())
                        .ifPresentOrElse(eventType -> {
                                    switch (eventType) {
                                        case LOGIN, LOGIN_ERROR: {
                                            userEventHandler.login(event, eventType);
                                        }
                                        case LOGOUT, LOGOUT_ERROR: {
                                            userEventHandler.logout(event, eventType);
                                        }
                                        case REGISTER, REGISTER_ERROR: {
                                            userEventHandler.register(event, eventType);
                                        }
                                        case RESET_PASSWORD, RESET_PASSWORD_ERROR: {
                                            userEventHandler.resetPassword(event, eventType);
                                        }
                                    }
                                },
                                () -> log.error("[KEYCLOAK-SPI] Received event with unknown type: {}", eventAsJson)
                        );
            }
        } catch (final JsonMappingException exception) {
            log.error("[KEYCLOAK-SPI] Keycloak event cannot be mapped into JSON with message: {}. EventID: {}, Event: {}", exception.getMessage(), event.getId(), event, exception);
            throw new KeycloakSpiException(KeycloakSpiExceptionMessage.KEYCLOAK_EVENT_JSON_MAPPING, exception);
        } catch (final KeycloakSpiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("[KEYCLOAK-SPI] Unknown error occurred during Keycloak event handling in SPI: {}", exception.getMessage(), exception);
            throw new KeycloakSpiException(KeycloakSpiExceptionMessage.UNKNOWN_ERROR, exception);
        }
    }

    @Override
    public void onEvent(final AdminEvent adminEvent, final boolean includeRepresentation) {

    }

    @Override
    public void close() {

    }

}
