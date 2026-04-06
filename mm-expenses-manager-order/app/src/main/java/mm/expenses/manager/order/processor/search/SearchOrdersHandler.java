package mm.expenses.manager.order.processor.search;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.*;
import mm.expenses.manager.order.processor.decorator.OrderClassicMapperToResponse;
import mm.expenses.manager.order.processor.decorator.OrderClassicMapperToResponseWithConversionDecorator;
import mm.expenses.manager.order.price.PriceConverter;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Handler responsible for search orders.
 */
@Slf4j
@Component
class SearchOrdersHandler extends OrderHandler {

    private final OrderSpecificationHandler specificationHandler;

    SearchOrdersHandler(final OrderRepository repository, final OrderMapper mapper, final PriceConverter priceConverter, final OrderSpecificationHandler specificationHandler) {
        super(repository, mapper, priceConverter);
        this.specificationHandler = specificationHandler;
    }

    @Override
    public Type getType() {
        return Type.SEARCH;
    }

    @Override
    public Response handle(final Request request) {
        final var chain = ChainCommandExecution.build(
                new SearchOrdersPrepareSpecificationCriterias(repository, specificationHandler),
                new SearchOrdersByCriteria(repository)
        );

        return of((Page<Order>) chain.handleRequest(request.request()));
    }

    @Override
    public Response handleDecorated(final Request request) {
        try {
            final var pagedOrders = handle(request);
            final var decorator = request.isShouldConvertCurrency()
                    ? new OrderClassicMapperToResponseWithConversionDecorator(priceConverter, new OrderClassicMapperToResponse(mapper), request.shouldConvertCurrency())
                    : new OrderClassicMapperToResponse(mapper);

            final var result = decorator.decorate(pagedOrders.pagedResponse());

            return of(pagedOrders.pagedResponse(), mapper.mapToPageResponse(result));
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown orders search error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDERS_CANNOT_BE_FOUND, exception);
        }
    }

}
