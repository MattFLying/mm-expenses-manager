package mm.expenses.manager.keycloak.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.expenses.manager.common.utils.security.Permission;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;

    private UUID keycloakId;

    private String username;

    private String email;

    private Map<String, Object> details = new HashMap<>();

    private String createdAt;

    private String lastModifiedAt;

    private Map<String, Set<Permission>> permissions = new HashMap<>();

}
