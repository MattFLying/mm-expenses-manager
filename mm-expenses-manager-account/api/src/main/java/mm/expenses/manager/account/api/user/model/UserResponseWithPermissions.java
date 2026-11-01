package mm.expenses.manager.account.api.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import mm.expenses.manager.common.utils.security.UserPermissions;

/**
 * Special API user response type with additionally present permissions to be used in
 * system API calls.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserResponseWithPermissions extends UserResponse {

    private UserPermissions permissions;

}
