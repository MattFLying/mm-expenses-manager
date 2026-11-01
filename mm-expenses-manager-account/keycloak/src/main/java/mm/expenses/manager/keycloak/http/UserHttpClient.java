package mm.expenses.manager.keycloak.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.keycloak.InternalProperties;
import mm.expenses.manager.keycloak.exception.KeycloakSpiException;
import mm.expenses.manager.keycloak.exception.KeycloakSpiExceptionMessage;
import mm.expenses.manager.keycloak.model.UserResponse;
import mm.expenses.manager.keycloak.request.UserRequestSender;
import mm.expenses.manager.keycloak.model.UserRegisteredModel;
import org.keycloak.events.Event;
import org.keycloak.models.UserModel;

import java.net.http.HttpRequest;
import java.util.UUID;

/**
 * Sends Keycloak events mapped to expected DTO models to be processed via specific HTTP endpoints.
 */
@Slf4j
public class UserHttpClient extends InternalHttpClient implements UserRequestSender {

    private static final String JSON_CONTENT_TYPE = "application/json";

    private final String usersUrl;

    public UserHttpClient(final ObjectMapper mapper, final InternalProperties internalProperties) {
        super(mapper, internalProperties);

        this.usersUrl = internalProperties.getAccountServiceUrl() + "/users";
    }

    /**
     * Calls user's registration API endpoint to process user creation in the system based on
     * already registered user directly on Keycloak side
     */
    @Override
    public void registerUser(final Event event, final UserModel user) {
        try {
            final var jsonPayload = mapper.writeValueAsString(new UserRegisteredModel(event, user));
            final var request = buildRequest(this.usersUrl)
                    .header("Content-Type", JSON_CONTENT_TYPE)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            sendAsync(request);
        } catch (final JsonProcessingException exception) {
            log.error("[KEYCLOAK-SPI] Cannot map registered user's data to send event via HTTP. Error message: {}, EventID: {}", exception.getMessage(), event.getId(), exception);
        } catch (final Exception exception) {
            log.error("[KEYCLOAK-SPI] Unknown error occurred during sending user registration event via Kafka. EventID: {}, ErrorMessage: {}", event.getId(), exception.getMessage(), exception);
        }
    }

    /**
     * Fetch user by given ID through API endpoint.
     * Method is safe for possible delays e.g. during registration process, so in case
     * user registered already and TokenMapper probably won't be available to fetch the user at the first try
     * because of account-service needs to process the Kafka registration event first - and in such case the
     * simple retry mechanism is useful. Also, this mechanism apply to any other case and there is always
     * specified max number of attempts to do not do it infinitely.
     */
    @Override
    public UserResponse getUser(final UUID id) {
        var maxAttemptsToFetchUser = 3; // max possible tries, 3 is the optimal option
        var delayBetweenCallsInMs = 300; // starts with 300ms delay before next try

        for (var attempt = 1; attempt <= maxAttemptsToFetchUser; attempt++) {
            try {
                final var request = buildRequest(String.format("%s/%s", this.usersUrl, id.toString()))
                        .header("Content-Type", JSON_CONTENT_TYPE)
                        .GET()
                        .build();
                final var response = send(request);

                if (response.statusCode() == 200) {
                    return mapper.readValue(response.body(), UserResponse.class);
                } else {
                    // in case of any error we need to retry fetching user but log this try to do not miss it in any case
                    // this is helpful e.g. in case when user registered in the system right now, so we need to wait some time more
                    // before processing to do not return error on the Swagger UI

                    log.warn("[KEYCLOAK-SPI] Cannot fetch user of ID: {}. [attempt ({}/{})]", id, attempt, maxAttemptsToFetchUser);
                }
            } catch (final Exception exception) {
                log.warn("[KEYCLOAK-SPI] Cannot fetch user of ID: {}. [attempt ({}/{})]. Error message: {}", id, attempt, maxAttemptsToFetchUser, exception.getMessage(), exception);
            }

            // in case that was the last attempt just break the loop
            if (attempt == maxAttemptsToFetchUser) {
                break;
            }

            // wait expected time before next try
            try {
                Thread.sleep(delayBetweenCallsInMs);
            } catch (final InterruptedException exception) {
                Thread.currentThread().interrupt();
                log.warn("[KEYCLOAK-SPI] Thread interrupted during fetching user ID={} attempts ({}/{}) with error message: {}", id, attempt, maxAttemptsToFetchUser, exception.getMessage(), exception);
                throw new RuntimeException("[KEYCLOAK-SPI] Thread interrupted during fetching user attempts", exception);
            }

            // increase next delay in ms before next attempt (e.g. from 300ms to 600ms)
            delayBetweenCallsInMs *= 2;
        }
        log.error("[KEYCLOAK-SPI] Cannot fetch user: {}", id);
        throw new KeycloakSpiException(KeycloakSpiExceptionMessage.USER_CANNOT_BE_DOWNLOADED_INTERNALLY);
    }

    /**
     * HTTP handler is for the case when there are some issues with Kafka handler, so it has to be '1' alternative priority.
     */
    @Override
    public int priority() {
        return 1;
    }

}
