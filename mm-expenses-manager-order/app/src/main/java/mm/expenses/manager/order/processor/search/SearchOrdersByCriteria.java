package mm.expenses.manager.order.processor.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.postgresql.specification.HandledSpecificationResult;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import org.springframework.data.domain.Page;

import java.util.Objects;

/**
 * Step in chain of search {@link Order}s.
 * Execute prepared criterias to search expected orders.
 */
@Slf4j
@RequiredArgsConstructor
final class SearchOrdersByCriteria extends SearchOrdersChain {

    private final OrderRepository repository;

    @Override
    public void setNextHandler(final ChainCommandExecution<EntityFilter, Page<Order>> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public Page<Order> handleRequest(final EntityFilter queryFilter) {
        try {
            Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

            final var specificationResultOpt = context.getArgument(SearchOrdersPrepareSpecificationCriterias.SPECIFICATION_RESULT_KEY);
            if (specificationResultOpt.isPresent()) {
                final var specificationResult = (HandledSpecificationResult<Order>) specificationResultOpt.get();
                final var pagedOrders = repository.findAll(specificationResult.specification(), specificationResult.pageable());

                if (hasNext()) {
                    // This step should not ever happen but if there is any possibility to do anything after save in database
                    // then there is a proper place to do it.
                    return next.handleRequest(queryFilter);
                } else {
                    return pagedOrders;
                }

            } else {
                log.error("Cannot search orders because of one of required parameters is null. specificationResultOpt={}", specificationResultOpt);
                throw new ApiConflictException(OrderExceptionMessage.ORDERS_CANNOT_BE_FOUND);
            }
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown orders search error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDERS_CANNOT_BE_FOUND, exception);
        }
    }

}
