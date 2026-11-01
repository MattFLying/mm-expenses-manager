package mm.expenses.manager.account.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.async.message.UserRegistrationMessage;
import org.springframework.stereotype.Component;

/**
 * Simple mapper from text value from Keycloak event into expected data models.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class UserMessageMapper {

    private final ObjectMapper objectMapper;

    public UserRegistrationMessage registration(final String message) {
        try {
            return objectMapper.readValue(message, UserRegistrationMessage.class);
        } catch (final JsonProcessingException exception) {
            log.error("Error occurred during mapping user registration message={}", message, exception);
            throw new RuntimeException(exception);
        } catch (final Exception exception) {
            log.error("Unknown error occurred during mapping user registration message={}", message, exception);
            throw new RuntimeException(exception);
        }
    }

}
