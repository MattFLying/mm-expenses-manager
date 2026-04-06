package mm.expenses.manager.order.processor.find;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.*;
import mm.expenses.manager.order.processor.decorator.OrderClassicMapperToResponse;
import mm.expenses.manager.order.processor.decorator.OrderClassicMapperToResponseWithConversionDecorator;
import mm.expenses.manager.order.price.PriceConverter;
import org.springframework.stereotype.Component;

/**
 * Handler responsible for finding single order.
 */
@Slf4j
@Component
class FindOrderHandler extends OrderHandler {

    FindOrderHandler(final OrderRepository repository, final OrderMapper mapper, final PriceConverter priceConverter) {
        super(repository, mapper, priceConverter);
    }

    @Override
    public Type getType() {
        return Type.FIND;
    }

    @Override
    public Response handle(final Request request) {
        final var chain = new FindOrderById(repository, request.isDeleted());
        final var response = chain.handleRequest(request.id());

        return of(response);
    }

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
            log.error("Unknown find single order error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(request.id()), exception);
        }
    }

}
