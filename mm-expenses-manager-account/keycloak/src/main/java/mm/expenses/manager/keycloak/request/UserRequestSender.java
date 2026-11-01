package mm.expenses.manager.keycloak.request;

import mm.expenses.manager.keycloak.model.UserResponse;
import org.keycloak.events.Event;
import org.keycloak.models.UserModel;

import java.util.UUID;

/**
 * User's request sender.
 */
public interface UserRequestSender extends RequestSender {

    /**
     * Implementation of sending request when received event from Keycloak related with user registration
     */
    void registerUser(final Event event, final UserModel user);

    UserResponse getUser(final UUID id);

}
