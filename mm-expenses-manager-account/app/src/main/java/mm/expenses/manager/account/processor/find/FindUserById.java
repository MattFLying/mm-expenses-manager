package mm.expenses.manager.account.processor.find;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserRepository;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;

import java.util.Objects;
import java.util.UUID;

/**
 * Step in chain of find specific {@link User} by its id or keycloak id.
 */
@Slf4j
@RequiredArgsConstructor
final class FindUserById extends FindUserByIdChain {

    private final UserRepository repository;
    private final Boolean isDeleted;

    @Override
    public void setNextHandler(final ChainCommandExecution<UUID, User> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public User handleRequest(final UUID id) {
        try {
            final var isDeletedFlag = Objects.nonNull(isDeleted) ? isDeleted : false;
            final var foundOrder = repository.findByIdOrKeycloakIdAndIsDeleted(id, id, isDeletedFlag)
                    .orElseThrow(() -> new ApiNotFoundException(UserExceptionMessage.USER_NOT_FOUND.withParameters(id)));

            if (hasNext()) {
                // This step should not ever happen but if there is any possibility to do anything after save in database
                // then there is a proper place to do it.
                return next.handleRequest(id);
            } else {
                return foundOrder;
            }
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown find single user error occurred.", exception);
            throw new ApiConflictException(UserExceptionMessage.USER_NOT_FOUND.withParameters(id), exception);
        }
    }

}
