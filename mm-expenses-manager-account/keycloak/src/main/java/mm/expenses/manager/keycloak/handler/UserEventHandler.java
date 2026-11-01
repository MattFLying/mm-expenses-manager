package mm.expenses.manager.keycloak.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.keycloak.InternalProperties;
import mm.expenses.manager.keycloak.request.RequestSender;
import mm.expenses.manager.keycloak.exception.KeycloakSpiException;
import mm.expenses.manager.keycloak.exception.KeycloakSpiExceptionMessage;
import mm.expenses.manager.keycloak.http.UserHttpClient;
import mm.expenses.manager.keycloak.kafka.UserKafkaSender;
import mm.expenses.manager.keycloak.request.UserRequestSender;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Dedicated handler of user's event from Keycloak to be specifically processed through the system.
 */
@Slf4j
public class UserEventHandler {

    private final KeycloakSession session;
    private final UserRequestSender sender;

    public UserEventHandler(final KeycloakSession session, final ObjectMapper objectMapper, final KafkaProducer<String, String> producer, final InternalProperties internalProperties) {
        this.session = session;
        this.sender = getRequestSender(
                List.of(
                        new UserKafkaSender(producer, objectMapper),
                        new UserHttpClient(objectMapper, internalProperties)
                )
        );
    }

    /**
     * Handles user's log in into the system event to log it.
     */
    public void login(final Event event, final EventType eventType) {
        try {
            switch (eventType) {
                case LOGIN: {
                    log.info("[KEYCLOAK-SPI] User login successfully. EventID: {}, UserID: {}", event.getId(), event.getUserId());
                }
                case LOGIN_ERROR: {
                    final var details = event.getDetails();
                    final var error = event.getError();

                    if (StringUtils.isNotBlank(error)) {
                        log.error("[KEYCLOAK-SPI] User login error with message: {}. EventID: {}, UserID: {}, Details: {}", error, event.getId(), event.getUserId(), details);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[KEYCLOAK-SPI] ERROR: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles user's log out from the system event to log it.
     */
    public void logout(final Event event, final EventType eventType) {
        switch (eventType) {
            case LOGOUT: {
                log.info("[KEYCLOAK-SPI] User logout successfully. EventID: {}, UserID: {}", event.getId(), event.getUserId());
            }
            case LOGOUT_ERROR: {
                final var details = event.getDetails();
                final var error = event.getError();

                if (StringUtils.isNotBlank(error)) {
                    log.debug("[KEYCLOAK-SPI] User logout error with message: {}. EventID: {}, UserID: {}, Details: {}", error, event.getId(), event.getUserId(), details);
                }
            }
        }
    }

    /**
     * Handles user's registration in the system event to log it.
     */
    public void register(final Event event, final EventType eventType) {
        final var user = getUserById(event.getUserId());

        switch (eventType) {
            case REGISTER: {
                sender.registerUser(event, user);
            }
            case REGISTER_ERROR: {
                final var details = event.getDetails();
                final var error = event.getError();

                if (StringUtils.isNotBlank(error)) {
                    log.error("[KEYCLOAK-SPI] User register error with message: {}. EventID: {}, UserID: {}, Details: {}", error, event.getId(), event.getUserId(), details);
                }
            }
        }
    }

    /**
     * Handles user's reset password event to log it.
     */
    public void resetPassword(final Event event, final EventType eventType) {
        switch (eventType) {
            case RESET_PASSWORD: {
                log.info("[KEYCLOAK-SPI] User reset password successfully. EventID: {}, UserID: {}", event.getId(), event.getUserId());
            }
            case RESET_PASSWORD_ERROR: {
                final var details = event.getDetails();
                final var error = event.getError();

                if (StringUtils.isNotBlank(error)) {
                    log.error("[KEYCLOAK-SPI] User reset password error with message: {}. EventID: {}, UserID: {}, Details: {}", error, event.getId(), event.getUserId(), details);
                }
            }
        }
    }

    /**
     * Prioritizing {@link UserRequestSender} and return specific sender based on the highest priority.
     */
    private UserRequestSender getRequestSender(final List<UserRequestSender> senders) {
        return senders.stream()
                .filter(RequestSender::isAvailable)
                .min(Comparator.comparing(RequestSender::priority))
                .orElseThrow(() -> new KeycloakSpiException(KeycloakSpiExceptionMessage.REQUEST_SENDER_UNAVAILABLE));
    }

    /**
     * Find user in the Keycloak by given user id.
     *
     * @param userId - id of user in the Keycloak
     */
    private UserModel getUserById(final String userId) {
        if (Objects.nonNull(userId)) {
            return session.users().getUserById(session.getContext().getRealm(), userId);
        }
        log.error("[KEYCLOAK-SPI] User does not exists in Keycloak database or id from event is null. UserID from event: {}", userId);
        throw new KeycloakSpiException(KeycloakSpiExceptionMessage.USER_DOES_NOT_EXISTS_IN_KEYCLOAK);
    }

}
