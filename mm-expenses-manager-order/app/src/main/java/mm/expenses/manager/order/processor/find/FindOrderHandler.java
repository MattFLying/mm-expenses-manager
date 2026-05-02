package mm.expenses.manager.order.processor.find;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
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
        return Type.FIND_ORDER;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        if (request instanceof OrderHandler.Request findRequest) {
            final var chain = new FindOrderById(repository, findRequest.isDeleted());
            return Response.builder()
                    .response(chain.handleRequest(findRequest.getId()))
                    .build();
        }
        throw new ProcessorHandler.ProcessorHandlerException(OrderExceptionMessage.ORDER_NOT_FOUND.getMessage());
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        try {
            if (request instanceof OrderHandler.Request findRequest) {
                final var response = handle(findRequest);
                final var decorator = findRequest.isShouldConvertCurrency()
                        ? new OrderClassicMapperToResponseWithConversionDecorator(priceConverter, new OrderClassicMapperToResponse(mapper), findRequest.isShouldConvertCurrency())
                        : new OrderClassicMapperToResponse(mapper);

                if (response instanceof OrderHandler.Response order) {
                    final var orderResponse = order.mapResponse(Order.class);
                    return Response.builder()
                            .response(orderResponse)
                            .decoratedResponse(decorator.decorate(orderResponse))
                            .build();
                }
            }
            throw new ProcessorHandler.ProcessorHandlerException(OrderExceptionMessage.ORDER_NOT_FOUND.getMessage());
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown find single order error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(request.getId()), exception);
        }
    }

}
