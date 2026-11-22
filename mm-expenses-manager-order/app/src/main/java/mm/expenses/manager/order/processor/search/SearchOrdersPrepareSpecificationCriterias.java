package mm.expenses.manager.order.processor.search;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.web.exception.security.ApiForbiddenException;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import org.springframework.data.domain.Page;

import java.util.Objects;

/**
 * Step in chain of search {@link Order}s.
 * Prepares specification criterias to find expected orders.
 */
@Slf4j
final class SearchOrdersPrepareSpecificationCriterias extends SearchOrdersChain {

    final static String SPECIFICATION_RESULT_KEY = "specificationResult";

    private final OrderSpecificationHandler specificationHandler;

    SearchOrdersPrepareSpecificationCriterias(final OrderRepository repository, final OrderSpecificationHandler specificationHandler) {
        super(repository);

        this.specificationHandler = specificationHandler;
    }

    @Override
    public Page<Order> handleRequest(final EntityFilter queryFilter) {
        try {
            Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

            checkPermissions((OrderFilter) queryFilter);
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

    private void checkPermissions(final OrderFilter orderFilter) {
        if (isSecured()) {
            final var permissions = getPermissions();

            if (orderFilter.shouldSearchAllOrders()) {
                if (!permissions.contains(this.fullPermission)) {
                    throw new ApiForbiddenException(OrderExceptionMessage.ORDER_NO_PERMISSION);
                }
            } else {
                if (!permissions.contains(this.ownPermission)) {
                    throw new ApiForbiddenException(OrderExceptionMessage.ORDER_NO_PERMISSION);
                }
            }
        }
    }

}
