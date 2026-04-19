package mm.expenses.manager.product.processor.delete.many;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.processor.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Handler responsible for multiple products deletion.
 */
@Component
class DeleteManyProductsHandler extends ProductHandler {

    private final ProductAsyncHandler asyncHandler;

    DeleteManyProductsHandler(final ProductRepository repository, final ProductMapper mapper, final PriceConverter priceConverter, final ProductAsyncHandler asyncHandler) {
        super(repository, mapper, priceConverter);

        this.asyncHandler = asyncHandler;
    }

    @Override
    public Type getType() {
        return Type.DELETE_MANY;
    }

    @Override
    public Response handle(final Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new FindProductsToRemove(repository, asyncHandler, now),
                new DeleteMultipleProducts(repository, asyncHandler, now),
                new SaveDeletedProducts(repository, asyncHandler),
                new AsyncProductsDelete(asyncHandler)
        );

        return of((List<Product>) chain.handleRequest(request.request()));
    }

    @Override
    public Response handleDecorated(final Request request) {
        return null;
    }

}
