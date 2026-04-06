package mm.expenses.manager.order.processor.delete.single;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.processor.*;
import mm.expenses.manager.order.price.PriceConverter;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Handler responsible for single order deletion.
 */
@Component
class DeleteSingleOrderHandler extends OrderHandler {

    DeleteSingleOrderHandler(final OrderRepository repository, final OrderMapper mapper, final PriceConverter priceConverter) {
        super(repository, mapper, priceConverter);
    }

    @Override
    public Type getType() {
        return Type.DELETE;
    }

    @Override
    public Response handle(final Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new FindOrderToRemove(repository, now),
                new DeleteSingleOrder(repository, now),
                new SaveDeletedOrder(repository)
        );

        return of((Order) chain.handleRequest(request.request()));
    }

    @Override
    public Response handleDecorated(final Request request) {
        return null;
    }

}
