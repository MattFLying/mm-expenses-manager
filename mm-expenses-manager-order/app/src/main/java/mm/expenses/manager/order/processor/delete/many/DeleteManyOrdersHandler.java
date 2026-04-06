package mm.expenses.manager.order.processor.delete.many;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.processor.Order;
import mm.expenses.manager.order.processor.OrderHandler;
import mm.expenses.manager.order.processor.OrderMapper;
import mm.expenses.manager.order.processor.OrderRepository;
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
        return Type.DELETE_MANY;
    }

    @Override
    public Response handle(final Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new FindOrdersToRemove(repository, now),
                new DeleteMultipleOrders(repository, now),
                new SaveDeletedOrders(repository)
        );

        return of((List<Order>) chain.handleRequest(request.request()));
    }

    @Override
    public Response handleDecorated(final Request request) {
        return null;
    }

}
