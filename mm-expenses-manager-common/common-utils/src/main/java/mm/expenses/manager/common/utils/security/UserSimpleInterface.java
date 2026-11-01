package mm.expenses.manager.common.utils.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.UUID;

/**
 * Simple user interface to be used in connection with keycloak users and available permissions.
 */
public interface UserSimpleInterface<T extends UserSimpleInterface.SimpleUser> {

    Optional<T> findByKeycloakId(final UUID keycloakId);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class SimpleUser {
        private UUID id;
        private UserPermissions permissions;
    }

}
