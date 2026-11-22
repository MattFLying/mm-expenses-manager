package mm.expenses.manager.common.utils.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines user permissions to be used within the application to protect access to different resources.
 */
@Getter
@RequiredArgsConstructor
public enum Permission {
    // account
    getOwnAccountInfo(PermissionGroup.ACCOUNT, true),
    updateOwnAccountInfo(PermissionGroup.ACCOUNT, true),
    deleteOwnAccountInfo(PermissionGroup.ACCOUNT, true),

    getUserInfo(PermissionGroup.ACCOUNT),
    getUsersInfo(PermissionGroup.ACCOUNT, true),

    updateUserInfo(PermissionGroup.ACCOUNT),
    deleteUserInfo(PermissionGroup.ACCOUNT),

    // order
    getOwnOrderInfo(PermissionGroup.ORDER, true),
    getOwnOrdersInfo(PermissionGroup.ORDER, true),
    createOwnOrder(PermissionGroup.ORDER, true),
    updateOwnOrder(PermissionGroup.ORDER, true),
    deleteOwnOrder(PermissionGroup.ORDER, true),

    getOrderInfo(PermissionGroup.ORDER),
    getOrdersInfo(PermissionGroup.ORDER),
    createOrder(PermissionGroup.ORDER),
    updateOrder(PermissionGroup.ORDER),
    deleteOrder(PermissionGroup.ORDER);

    private final PermissionGroup group;
    private final boolean isInitial;

    Permission(final PermissionGroup group) {
        this(group, false);
    }

    public String getRole() {
        return String.format("%s_%s", getGroup().name().toUpperCase(), name());
    }

    public String getPermission() {
        return name();
    }

}
