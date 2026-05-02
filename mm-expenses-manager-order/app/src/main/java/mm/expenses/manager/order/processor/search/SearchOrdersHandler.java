package mm.expenses.manager.order.processor.search;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderMapper;
import mm.expenses.manager.order.core.OrderRepository;
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
        return Type.SEARCH_ORDERS;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        final var chain = ChainCommandExecution.build(
                new SearchOrdersPrepareSpecificationCriterias(repository, specificationHandler),
                new SearchOrdersByCriteria(repository)
        );
        return Response.builder()
                .pagedResponse((Page<Order>) chain.handleRequest(request.getRequest()))
                .build();
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        try {
            if (request instanceof OrderHandler.Request searchRequest) {
                final var response = handle(searchRequest);
                final var decorator = searchRequest.isShouldConvertCurrency()
                        ? new OrderClassicMapperToResponseWithConversionDecorator(priceConverter, new OrderClassicMapperToResponse(mapper), searchRequest.isShouldConvertCurrency())
                        : new OrderClassicMapperToResponse(mapper);

                if (response instanceof OrderHandler.Response order) {
                    final var pagedOrders = order.getPagedResponse();
                    final var pagedOrderResponse = decorator.decorate(pagedOrders);
                    return Response.builder()
                            .pagedResponse(pagedOrders)
                            .decoratedPagedResponse(mapper.mapToPageResponse(pagedOrderResponse))
                            .build();
                }
            }
            throw new ProcessorHandler.ProcessorHandlerException(OrderExceptionMessage.ORDERS_CANNOT_BE_FOUND.getMessage());
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown orders search error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDERS_CANNOT_BE_FOUND, exception);
        }
    }

}
