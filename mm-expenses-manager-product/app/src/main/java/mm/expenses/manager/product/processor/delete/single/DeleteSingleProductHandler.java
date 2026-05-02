package mm.expenses.manager.product.processor.delete.single;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import mm.expenses.manager.product.core.ProductAsyncHandler;
import mm.expenses.manager.product.core.ProductMapper;
import mm.expenses.manager.product.core.ProductRepository;
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
        return Type.DELETE_SINGLE_PRODUCT;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new FindProductToRemove(repository, asyncHandler, now),
                new DeleteSingleProduct(repository, asyncHandler, now),
                new SaveDeletedProduct(repository, asyncHandler),
                new AsyncProductDelete(asyncHandler)
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
