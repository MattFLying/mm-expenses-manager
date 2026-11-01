package mm.expenses.manager.keycloak.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.keycloak.events.Event;
import org.keycloak.models.UserModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * User registered model mapped from the Keycloak event to be processed further.
 */
@Setter
@Getter
public class UserRegisteredModel extends DefaultEventModel {

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

    public UserRegisteredModel(final Event event, final UserModel user) {
        super(event);

        this.userId = event.getUserId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.isEmailVerified = user.isEmailVerified();
        this.isEnabled = user.isEnabled();

        this.attributes = Objects.nonNull(user.getAttributes()) ? user.getAttributes() : new HashMap<>();
    }

    public Boolean isEnabled() {
        return isEnabled != null && isEnabled;
    }

}
