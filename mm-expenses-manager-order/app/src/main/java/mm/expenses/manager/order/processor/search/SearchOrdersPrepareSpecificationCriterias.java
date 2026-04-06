package mm.expenses.manager.order.processor.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.Order;
import mm.expenses.manager.order.processor.OrderRepository;
import org.springframework.data.domain.Page;

import java.util.Objects;

/**
 * Step in chain of search {@link Order}s.
 * Prepares specification criterias to find expected orders.
 */
@Slf4j
@RequiredArgsConstructor
final class SearchOrdersPrepareSpecificationCriterias extends SearchOrdersChain {

    final static String SPECIFICATION_RESULT_KEY = "specificationResult";

    private final OrderRepository repository;
    private final OrderSpecificationHandler specificationHandler;

    @Override
    public Page<Order> handleRequest(final EntityFilter queryFilter) {
        try {
            Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

            final var filterParameters = queryFilter.buildQueryParams();
            final var specificationResult = specificationHandler.handle(filterParameters, queryFilter.getAdditionalCriteriaParametersAsArray());
            context.addArgument(SPECIFICATION_RESULT_KEY, specificationResult);

            if (!hasNext()) {
                setNextHandler(new SearchOrdersByCriteria(repository));
            }
            return next.handleRequest(queryFilter);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown orders search error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDERS_CANNOT_BE_FOUND, exception);
        }
    }

}
