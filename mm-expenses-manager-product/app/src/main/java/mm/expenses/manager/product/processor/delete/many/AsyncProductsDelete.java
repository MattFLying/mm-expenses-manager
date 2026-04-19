package mm.expenses.manager.product.processor.delete.many;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductAsyncHandler;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Step in chain of multiple {@link Product}s deletion.
 * Process asynchronous communication when products are deleted and stored in database.
 */
@Slf4j
@RequiredArgsConstructor
final class AsyncProductsDelete extends DeleteMultipleProductsChain<Set<UUID>> {

    private final ProductAsyncHandler asyncHandler;

    @Override
    public void setNextHandler(final ChainCommandExecution<Set<UUID>, List<Product>> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public List<Product> handleRequest(final Set<UUID> ids) {
        try {
            final var savedOpt = context.getArgument(Context.SAVED_KEY);
            if (savedOpt.isPresent()) {
                final var saved = (List<Product>) savedOpt.get();
                saved.forEach(product -> asyncHandler.sendProductMessage(product, AsyncKafkaOperation.DELETE));

                if (hasNext()) {
                    // This step should not ever happen but if there is any possibility to do anything after save in database
                    // then there is a proper place to do it.
                    return next.handleRequest(ids);
                } else {
                    return saved;
                }
            } else {
                log.error("Cannot delete multiple products because of one of required parameters is null.");
                throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_DELETED);
            }
        } catch (final ApiException exception) {
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_DELETED, exception);
        } catch (final Exception exception) {
            log.error("Unknown multiple products deletion error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_DELETED, exception);
        }
    }

}
