package mm.expenses.manager.order.processor.update;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.*;
import mm.expenses.manager.order.processor.decorator.OrderClassicMapperToResponse;
import mm.expenses.manager.order.processor.decorator.OrderClassicMapperToResponseWithConversionDecorator;
import mm.expenses.manager.order.price.PriceConverter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        return Type.UPDATE;
    }

    @Transactional
    @Override
    public Response handle(final Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new ValidateRequestedOrderUpdates(repository, productService, now, request.id()),
                new FindOrderToUpdate(repository, productService, now),
                new UpdateBasicOrderData(repository, productService, now),
                new ModifyOrderedProductsDuringOrderUpdate(repository, productService, now),
                new OrderSummaryUpdateDuringUpdate(repository, now),
                new SaveUpdatedOrder(repository)
        );
        return of((Order) chain.handleRequest(request.request()));
    }

    @Transactional
    @Override
    public Response handleDecorated(final Request request) {
        try {
            final var order = handle(request);
            final var decorator = request.isShouldConvertCurrency()
                    ? new OrderClassicMapperToResponseWithConversionDecorator(priceConverter, new OrderClassicMapperToResponse(mapper), request.shouldConvertCurrency())
                    : new OrderClassicMapperToResponse(mapper);

            return of(order.response(), decorator.decorate(order.response()));
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown order update error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED, exception);
        }
    }

}
