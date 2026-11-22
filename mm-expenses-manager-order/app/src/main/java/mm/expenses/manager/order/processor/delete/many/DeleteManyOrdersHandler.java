package mm.expenses.manager.order.processor.delete.many;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import mm.expenses.manager.common.web.chain.SecuredChainCommandExecution;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.OrderHandler;
import mm.expenses.manager.order.core.OrderMapper;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.price.PriceConverter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Handler responsible for multiple orders deletion.
 */
@Slf4j
@Component
class DeleteManyOrdersHandler extends OrderHandler {

    DeleteManyOrdersHandler(final OrderRepository repository, final OrderMapper mapper, final PriceConverter priceConverter) {
        super(repository, mapper, priceConverter);
    }

    @Override
    public Type getType() {
        return Type.DELETE_MANY_ORDERS;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        try {
            if (request instanceof OrderHandler.Request deleteRequest) {
                final var now = Instant.now();
                final var securityContext = deleteRequest.getSecurityContext();

                final var chain = SecuredChainCommandExecution.build(
                        securityContext,
                        new FindOrdersToRemove(repository, now),
                        new DeleteMultipleOrders(repository, now),
                        new SaveDeletedOrders(repository)
                );

                return Response.builder()
                        .listedResponse((List<Order>) chain.handleRequest(deleteRequest.getRequest()))
                        .build();
            }
            throw new ProcessorHandler.ProcessorHandlerException(OrderExceptionMessage.ORDER_CANNOT_BE_DELETED.getMessage());
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown orders deletion error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_DELETED, exception);
        }
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        return null;
    }

}
