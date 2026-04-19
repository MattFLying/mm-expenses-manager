package mm.expenses.manager.product.processor.delete.single;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.processor.*;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Handler responsible for single product deletion.
 */
@Component
class DeleteSingleProductHandler extends ProductHandler {

    private final ProductAsyncHandler asyncHandler;

    DeleteSingleProductHandler(final ProductRepository repository, final ProductMapper mapper, final mm.expenses.manager.product.currency.PriceConverter priceConverter, final ProductAsyncHandler asyncHandler) {
        super(repository, mapper, priceConverter);

        this.asyncHandler = asyncHandler;
    }

    @Override
    public Type getType() {
        return Type.DELETE;
    }

    @Override
    public Response handle(final Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new FindProductToRemove(repository, asyncHandler, now),
                new DeleteSingleProduct(repository, asyncHandler, now),
                new SaveDeletedProduct(repository, asyncHandler),
                new AsyncProductDelete(asyncHandler)
        );

        return of((Product) chain.handleRequest(request.request()));
    }

    @Override
    public Response handleDecorated(final Request request) {
        return null;
    }

}
