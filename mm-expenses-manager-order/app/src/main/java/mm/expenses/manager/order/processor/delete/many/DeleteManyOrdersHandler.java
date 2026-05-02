package mm.expenses.manager.order.processor.delete.many;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import mm.expenses.manager.order.core.Order;
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
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new FindOrdersToRemove(repository, now),
                new DeleteMultipleOrders(repository, now),
                new SaveDeletedOrders(repository)
        );
        return Response.builder()
                .listedResponse((List<Order>) chain.handleRequest(request.getRequest()))
                .build();
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        return null;
    }

}
