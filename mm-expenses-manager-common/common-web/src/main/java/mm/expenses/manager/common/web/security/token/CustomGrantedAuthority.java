package mm.expenses.manager.common.web.security.token;

import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.utils.security.PermissionGroup;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

/**
 * Custom {@link GrantedAuthority} implementation to handle permissions and their details created
 * during authentication in APIs usages.
 */
final class CustomGrantedAuthority implements GrantedAuthority {

    private final Permission permission;
    private final PermissionGroup group;
    private final String role;

    CustomGrantedAuthority(final Permission permission) {
        Assert.notNull(permission, "A granted permission is required");
        this.permission = permission;
        this.group = permission.getGroup();
        this.role = permission.getRole();
    }

    public PermissionGroup getGroup() {
        return this.group;
    }

    public Permission getPermission() {
        return this.permission;
    }

    public String getRole() {
        return this.role;
    }

    @Override
    public String getAuthority() {
        return this.permission.getPermission();
    }

    @Override
    public int hashCode() {
        return this.role.hashCode();
    }

    @Override
    public String toString() {
        return this.role;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof CustomGrantedAuthority grantedAuthority) {
            final var isPermissionEqual = this.permission.equals(grantedAuthority.getPermission());
            final var isPermissionGroupEqual = this.group.equals(grantedAuthority.getGroup());
            final var isRoleEqual = this.role.equals(grantedAuthority.getRole());

            return isPermissionEqual && isPermissionGroupEqual && isRoleEqual;
        }
        return false;
    }

}
