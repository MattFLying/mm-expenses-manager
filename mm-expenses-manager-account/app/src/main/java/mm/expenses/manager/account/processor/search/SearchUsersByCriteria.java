package mm.expenses.manager.account.processor.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserRepository;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.postgresql.specification.HandledSpecificationResult;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import org.springframework.data.domain.Page;

import java.util.Objects;

/**
 * Step in chain of search {@link User}s.
 * Execute prepared criterias to search expected users.
 */
@Slf4j
@RequiredArgsConstructor
final class SearchUsersByCriteria extends SearchUsersChain {

    private final UserRepository repository;

    @Override
    public void setNextHandler(final ChainCommandExecution<EntityFilter, Page<User>> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public Page<User> handleRequest(final EntityFilter queryFilter) {
        try {
            Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

            final var specificationResultOpt = context.getArgument(SearchUsersPrepareSpecificationCriterias.SPECIFICATION_RESULT_KEY);
            if (specificationResultOpt.isPresent()) {
                final var specificationResult = (HandledSpecificationResult<User>) specificationResultOpt.get();
                final var pagedUsers = repository.findAll(specificationResult.specification(), specificationResult.pageable());

                if (hasNext()) {
                    // This step should not ever happen but if there is any possibility to do anything after save in database
                    // then there is a proper place to do it.
                    return next.handleRequest(queryFilter);
                } else {
                    return pagedUsers;
                }

            } else {
                log.error("Cannot search users because of one of required parameters is null. specificationResultOpt={}", specificationResultOpt);
                throw new ApiConflictException(UserExceptionMessage.USERS_CANNOT_BE_FOUND);
            }
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown users search error occurred.", exception);
            throw new ApiConflictException(UserExceptionMessage.USERS_CANNOT_BE_FOUND, exception);
        }
    }

}
