package mm.expenses.manager.order.processor.delete.single;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import mm.expenses.manager.order.core.OrderMapper;
import mm.expenses.manager.order.core.OrderRepository;
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
        return Type.DELETE_SINGLE_ORDER;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new FindOrderToRemove(repository, now),
                new DeleteSingleOrder(repository, now),
                new SaveDeletedOrder(repository)
        );
        return Response.builder()
                .response(chain.handleRequest(request.getRequest()))
                .build();
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        return null;
    }

}
