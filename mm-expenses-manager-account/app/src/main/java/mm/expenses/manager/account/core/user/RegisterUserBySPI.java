package mm.expenses.manager.account.core.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.*;

/**
 * User registration message from Keycloak via REST API to be processed.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserBySPI implements CreateUserModel {

    @JsonProperty(value = "event_id")
    private String eventId;

    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "time")
    private Long time;

    @JsonProperty(value = "enabled")
    private Boolean isEnabled;

    @JsonProperty(value = "emailVerified")
    private Boolean isEmailVerified;

    @JsonProperty(value = "user_id")
    private String userId;

    @JsonProperty(value = "email")
    private String email;

    @JsonProperty(value = "username")
    private String username;

    @JsonProperty(value = "attributes")
    private RegisterUserAttributesBySPI attributes;

    public Instant getTimeAsInstant() {
        return Objects.nonNull(time) ? Instant.ofEpochMilli(time) : null;
    }

    @Override
    public String getRealmId() {
        return StringUtils.EMPTY;
    }

    @Override
    public String getRealmName() {
        return StringUtils.EMPTY;
    }

    @Override
    public Map<String, String> getDetails() {
        return Map.of();
    }

    /**
     * Map of available attributes taken from the Keycloak directly.
     */
    public static class RegisterUserAttributesBySPI extends HashMap<String, List<String>> {

        public Optional<String> getFirstName() {
            final var list = get("firstName");

            return getAvailableValue(list);
        }

        public Optional<String> getLastName() {
            final var list = get("lastName");

            return getAvailableValue(list);
        }

        public Optional<String> getEmail() {
            final var list = get("email");

            return getAvailableValue(list);
        }

        public Optional<String> getUsername() {
            final var list = get("username");

            return getAvailableValue(list);
        }

        private Optional<String> getAvailableValue(final List<String> list) {
            return CollectionUtils.isNotEmpty(list) ? Optional.of(list.get(0)) : Optional.empty();
        }

    }

}
