package mm.expenses.manager.order.processor.create;

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
        return Type.CREATE_ORDER;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new FindOrderedProducts(repository, productService, now),
                new MapOrderedProducts(repository, productService, now),
                new CalculatePrices(repository, now),
                new SaveCreatedOrder(repository, now)
        );
        return Response.builder()
                .response(chain.handleRequest(request.getRequest()))
                .build();
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        try {
            if (request instanceof OrderHandler.Request createRequest) {
                final var response = handle(createRequest);
                final var decorator = createRequest.isShouldConvertCurrency()
                        ? decorateWithCurrencyConversion(createRequest)
                        : new OrderClassicMapperToResponse(mapper);

                if (response instanceof OrderHandler.Response orderResponse) {
                    final var order = orderResponse.mapResponse(Order.class);
                    return Response.builder()
                            .response(order)
                            .decoratedResponse(decorator.decorate(order))
                            .build();
                }
            }
            throw new ProcessorHandler.ProcessorHandlerException(OrderExceptionMessage.ORDER_CANNOT_BE_CREATED.getMessage());
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
                request.isShouldConvertCurrency()
        );
    }

}
