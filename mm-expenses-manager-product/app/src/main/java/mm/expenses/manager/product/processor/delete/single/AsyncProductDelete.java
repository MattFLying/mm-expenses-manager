package mm.expenses.manager.product.processor.delete.single;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductAsyncHandler;

import java.util.UUID;

/**
 * Step in chain of single {@link Product} deletion.
 * Process asynchronous communication when product is deleted and stored in database.
 */
@Slf4j
@RequiredArgsConstructor
final class AsyncProductDelete extends DeleteProductByIdChain {

    private final ProductAsyncHandler asyncHandler;

    @Override
    public void setNextHandler(final ChainCommandExecution<UUID, Product> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public Product handleRequest(final UUID productId) {
        try {
            final var savedOpt = context.getArgument(Context.SAVED_KEY);
            if (savedOpt.isPresent()) {
                final var saved = (Product) savedOpt.get();
                asyncHandler.sendProductMessage(saved, AsyncKafkaOperation.DELETE);

                if (hasNext()) {
                    // This step should not ever happen but if there is any possibility to do anything after save in database
                    // then there is a proper place to do it.
                    return next.handleRequest(productId);
                } else {
                    return saved;
                }
            } else {
                log.error("Cannot delete single product because of one of required parameters is null.");
                throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_DELETED);
            }
        } catch (final ApiException exception) {
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_DELETED, exception);
        } catch (final Exception exception) {
            log.error("Unknown single product deletion error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_DELETED, exception);
        }
    }

}
