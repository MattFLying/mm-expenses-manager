package mm.expenses.manager.order.processor.update;

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
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Handler responsible for order update.
 */
@Slf4j
@Component
class UpdateOrderHandler extends OrderHandler {

    private final OrderedProductService productService;

    UpdateOrderHandler(final OrderRepository repository, final OrderMapper mapper, final PriceConverter priceConverter, final OrderedProductService productService) {
        super(repository, mapper, priceConverter);

        this.productService = productService;
    }

    @Override
    public Type getType() {
        return Type.UPDATE_ORDER;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new ValidateRequestedOrderUpdates(repository, productService, now, request.getId()),
                new FindOrderToUpdate(repository, productService, now),
                new UpdateBasicOrderData(repository, productService, now),
                new ModifyOrderedProductsDuringOrderUpdate(repository, productService, now),
                new OrderSummaryUpdateDuringUpdate(repository, now),
                new SaveUpdatedOrder(repository)
        );
        return Response.builder()
                .response(chain.handleRequest(request.getRequest()))
                .build();
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        try {
            if (request instanceof OrderHandler.Request updateRequest) {
                final var response = handle(updateRequest);
                final var decorator = updateRequest.isShouldConvertCurrency()
                        ? new OrderClassicMapperToResponseWithConversionDecorator(priceConverter, new OrderClassicMapperToResponse(mapper), updateRequest.isShouldConvertCurrency())
                        : new OrderClassicMapperToResponse(mapper);

                if (response instanceof OrderHandler.Response order) {
                    final var orderResponse = order.mapResponse(Order.class);
                    return Response.builder()
                            .response(orderResponse)
                            .decoratedResponse(decorator.decorate(orderResponse))
                            .build();
                }
            }
            throw new ProcessorHandler.ProcessorHandlerException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED.getMessage());
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown order update error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED, exception);
        }
    }

}
