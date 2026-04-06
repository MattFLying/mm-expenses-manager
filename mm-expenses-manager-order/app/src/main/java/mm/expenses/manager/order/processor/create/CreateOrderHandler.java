package mm.expenses.manager.order.processor.create;

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
 * Handler responsible for order creation.
 */
@Slf4j
@Component
class CreateOrderHandler extends OrderHandler {

    private final OrderedProductService productService;

    CreateOrderHandler(final OrderRepository repository, final OrderMapper mapper, final PriceConverter priceConverter, final OrderedProductService productService) {
        super(repository, mapper, priceConverter);

        this.productService = productService;
    }

    @Override
    public Type getType() {
        return Type.CREATE;
    }

    @Transactional
    @Override
    public Response handle(final Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new FindOrderedProducts(repository, productService, now),
                new MapOrderedProducts(repository, productService, now),
                new CalculatePrices(repository, now),
                new SaveCreatedOrder(repository, now)
        );
        return of((Order) chain.handleRequest(request.request()));
    }

    @Transactional
    @Override
    public Response handleDecorated(final Request request) {
        try {
            final var order = handle(request);
            final var decorator = request.isShouldConvertCurrency()
                    ? decorateWithCurrencyConversion(request)
                    : new OrderClassicMapperToResponse(mapper);

            return of(order.response(), decorator.decorate(order.response()));
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown order creation error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_CREATED, exception);
        }
    }

    private OrderClassicMapperToResponseWithConversionDecorator decorateWithCurrencyConversion(final Request request) {
        return new OrderClassicMapperToResponseWithConversionDecorator(
                priceConverter,
                new OrderClassicMapperToResponse(mapper),
                request.shouldConvertCurrency()
        );
    }

}
