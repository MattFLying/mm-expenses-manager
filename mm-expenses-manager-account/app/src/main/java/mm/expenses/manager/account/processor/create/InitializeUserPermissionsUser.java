package mm.expenses.manager.account.processor.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.core.user.CreateUserModel;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserRepository;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.utils.security.UserPermissions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Step in chain of new {@link User} creation.
 * Already prepared user to be stored in database now requires initial permissions to be set.
 */
@Slf4j
@RequiredArgsConstructor
final class InitializeUserPermissionsUser extends CreateUserChain {

    private final UserRepository repository;

    @Override
    public User handleRequest(final CreateUserModel message) {
        try {
            final var preparedUserOpt = context.getArgument(MapRegisteredUser.PREPARED_USER_TO_SAVE);

            if (preparedUserOpt.isPresent()) {
                final var preparedUser = (User) preparedUserOpt.get();

                initUserPermissions(preparedUser);

                if (!hasNext()) {
                    setNextHandler(new SaveCreatedUser(repository));
                }
                return next.handleRequest(message);
            } else {
                log.error("Cannot create new user because of required parameter is null: preparedUserOpt={}", preparedUserOpt);
                throw new ApiConflictException(UserExceptionMessage.USER_CANNOT_BE_CREATED);
            }
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown user creation error occurred.", exception);
            throw new ApiConflictException(UserExceptionMessage.USER_CANNOT_BE_CREATED, exception);
        }
    }

    private void initUserPermissions(final User user) {
        final var initialPermissions = Arrays.stream(Permission.values())
                .filter(Permission::isInitial)
                .collect(Collectors.groupingBy(Permission::getGroup));

        final var permissions = new UserPermissions();
        initialPermissions.forEach((group, permissionList) -> {
            permissions.put(group.name(), new HashSet<>(permissionList));
        });

        user.setPermissions(permissions);
    }

}
