package mm.expenses.manager.account.async.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.account.core.user.CreateUserModel;
import mm.expenses.manager.common.async.AsyncBinding;

import java.util.List;
import java.util.Map;

/**
 * User registration message from Keycloak via Kafka to be processed.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationMessage implements AsyncBinding, CreateUserModel {

    @JsonProperty(value = "event_id")
    private String eventId;

    @JsonProperty(value = "type")
    private String type;

    @JsonProperty(value = "realm_id")
    private String realmId;

    @JsonProperty(value = "realm_name")
    private String realmName;

    @JsonProperty(value = "time")
    private Long time;

    @JsonProperty(value = "user_id")
    private String userId;

    @JsonProperty(value = "email")
    private String email;

    @JsonProperty(value = "username")
    private String username;

    @JsonProperty(value = "is_email_verified")
    private Boolean isEmailVerified;

    @JsonProperty(value = "is_enabled")
    private Boolean isEnabled;

    @JsonProperty(value = "attributes")
    private Map<String, List<String>> attributes;

    @JsonProperty(value = "details")
    private Map<String, String> details;

    @Override
    public String producerBindingName() {
        return "keycloakSpiUserRegistration-out-0";
    }

    @Override
    public String consumerBindingName() {
        return "keycloakSpiUserRegistration-in-0";
    }

    @Override
    public String topicName() {
        return "keycloak-spi-user-registration";
    }

}
