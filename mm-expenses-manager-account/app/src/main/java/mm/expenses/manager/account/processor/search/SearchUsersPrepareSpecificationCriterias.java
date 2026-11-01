package mm.expenses.manager.account.processor.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserRepository;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import org.springframework.data.domain.Page;

import java.util.Objects;

/**
 * Step in chain of search {@link User}s.
 * Prepares specification criterias to find expected users.
 */
@Slf4j
@RequiredArgsConstructor
final class SearchUsersPrepareSpecificationCriterias extends SearchUsersChain {

    final static String SPECIFICATION_RESULT_KEY = "specificationResult";

    private final UserRepository repository;
    private final UserSpecificationHandler specificationHandler;

    @Override
    public Page<User> handleRequest(final EntityFilter queryFilter) {
        try {
            Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

            final var filterParameters = queryFilter.buildQueryParams();
            final var specificationResult = specificationHandler.handle(filterParameters, queryFilter.getAdditionalCriteriaParametersAsArray());
            context.addArgument(SPECIFICATION_RESULT_KEY, specificationResult);

            if (!hasNext()) {
                setNextHandler(new SearchUsersByCriteria(repository));
            }
            return next.handleRequest(queryFilter);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown users search error occurred.", exception);
            throw new ApiConflictException(UserExceptionMessage.USERS_CANNOT_BE_FOUND, exception);
        }
    }

}
