package mm.expenses.manager.account.processor.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.core.user.CreateUserModel;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserRepository;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiInternalErrorException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * Step in chain of new {@link User} creation.
 * Builds the expected {@link User} object and save it into database.
 */
@Slf4j
@RequiredArgsConstructor
final class SaveCreatedUser extends CreateUserChain {

    private final UserRepository repository;

    @Override
    public void setNextHandler(final ChainCommandExecution<CreateUserModel, User> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public User handleRequest(final CreateUserModel message) {
        try {
            final var preparedUserOpt = context.getArgument(MapRegisteredUser.PREPARED_USER_TO_SAVE);

            if (preparedUserOpt.isPresent()) {
                final var newUser = (User) preparedUserOpt.get();

                final var saved = repository.save(newUser);
                context.addArgument(ChainCommandExecution.Context.SAVED_KEY, saved);

                if (hasNext()) {
                    // This step should not ever happen but if there is any possibility to do anything after save in database
                    // then there is a proper place to do it.
                    return next.handleRequest(message);
                } else {
                    return saved;
                }
            } else {
                log.error("Cannot create new user because of required parameter is null: preparedUserOpt={}", preparedUserOpt);
                throw new ApiConflictException(UserExceptionMessage.USER_CANNOT_BE_CREATED);
            }
        } catch (final IllegalArgumentException exception) {
            log.error("User entity is null.", exception);
            throw new ApiConflictException(UserExceptionMessage.USER_CANNOT_BE_CREATED, exception);
        } catch (final OptimisticLockingFailureException exception) {
            log.error("User entity has optimistic lock failure.", exception);
            throw new ApiInternalErrorException(UserExceptionMessage.USER_CANNOT_BE_CREATED, exception);
        } catch (final ApiException exception) {
            throw new ApiConflictException(UserExceptionMessage.USER_CANNOT_BE_CREATED, exception);
        } catch (final Exception exception) {
            log.error("Unknown user creation error occurred.", exception);
            throw new ApiConflictException(UserExceptionMessage.USER_CANNOT_BE_CREATED, exception);
        }
    }

}
