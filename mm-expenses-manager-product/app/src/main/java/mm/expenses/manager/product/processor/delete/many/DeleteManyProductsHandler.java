package mm.expenses.manager.product.processor.delete.many;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductAsyncHandler;
import mm.expenses.manager.product.core.ProductMapper;
import mm.expenses.manager.product.core.ProductRepository;
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
        return Type.DELETE_MANY_PRODUCTS;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new FindProductsToRemove(repository, asyncHandler, now),
                new DeleteMultipleProducts(repository, asyncHandler, now),
                new SaveDeletedProducts(repository, asyncHandler),
                new AsyncProductsDelete(asyncHandler)
        );
        return Response.builder()
                .listedResponse((List<Product>) chain.handleRequest(request.getRequest()))
                .build();
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        return null;
    }

}
