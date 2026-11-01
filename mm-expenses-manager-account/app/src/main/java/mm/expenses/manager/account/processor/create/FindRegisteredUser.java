package mm.expenses.manager.account.processor.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.core.user.CreateUserModel;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserRepository;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Step in chain of new {@link User} creation.
 * Finds if requested user already exists.
 */
@Slf4j
@RequiredArgsConstructor
final class FindRegisteredUser extends CreateUserChain {

    final static String USER_KEYCLOAK_ID = "userKeycloakId";

    private final UserRepository repository;
    private final Instant creationTime;

    @Override
    public User handleRequest(final CreateUserModel message) {
        try {
            // this is ID from the keycloak directly - not internal id
            final var userId = message.getUserId();
            if (StringUtils.isBlank(userId)) {
                throw new ApiValidationException(UserExceptionMessage.USER_ID_CANNOT_BE_EMPTY);
            }

            final var userIdAsUuid = UUID.fromString(userId);
            final var user = repository.findByKeycloakId(userIdAsUuid);
            if (user.isPresent()) {
                throw new ApiValidationException(UserExceptionMessage.USER_ALREADY_EXISTS);
            }

            context.addArguments(Map.of(USER_KEYCLOAK_ID, userIdAsUuid));

            if (!hasNext()) {
                setNextHandler(new MapRegisteredUser(repository, creationTime));
            }
            return next.handleRequest(message);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown user creation error occurred.", exception);
            throw new ApiConflictException(UserExceptionMessage.USER_CANNOT_BE_CREATED, exception);
        }
    }

}
