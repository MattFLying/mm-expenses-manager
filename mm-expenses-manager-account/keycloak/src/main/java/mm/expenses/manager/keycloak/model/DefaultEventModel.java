package mm.expenses.manager.keycloak.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.keycloak.events.Event;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default object based on event received from Keycloak mapped with common default details to be processed further.
 */
@Setter
@Getter
public abstract class DefaultEventModel {

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

    @JsonProperty(value = "details")
    private Map<String, String> details;

    @JsonProperty(value = "timeAsInstant")
    private Instant timeAsInstant;

    public DefaultEventModel(final Event event) {
        this.eventId = event.getId();
        this.type = event.getType().name();
        this.realmId = event.getRealmId();
        this.realmName = event.getRealmName();
        this.time = event.getTime();

        this.details = Objects.nonNull(event.getDetails()) ? event.getDetails() : new HashMap<>();
        this.timeAsInstant = Objects.nonNull(this.time) ? Instant.ofEpochMilli(this.time) : null;
    }

}
