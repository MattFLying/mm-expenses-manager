package mm.expenses.manager.product.processor.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductAsyncHandler;

/**
 * Step in chain of {@link Product} update.
 * Process asynchronous communication when product is updated and stored in database.
 */
@Slf4j
@RequiredArgsConstructor
final class AsyncProductUpdate extends UpdateProductChain {

    private final ProductAsyncHandler asyncHandler;

    @Override
    public void setNextHandler(final ChainCommandExecution<UpdateProductRequest, Product> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public Product handleRequest(final UpdateProductRequest request) {
        try {
            final var savedOpt = context.getArgument(Context.SAVED_KEY);
            if (savedOpt.isPresent()) {
                final var saved = (Product) savedOpt.get();
                asyncHandler.sendProductMessage(saved, AsyncKafkaOperation.UPDATE);

                if (hasNext()) {
                    // This step should not ever happen but if there is any possibility to do anything after save in database
                    // then there is a proper place to do it.
                    return next.handleRequest(request);
                } else {
                    return saved;
                }
            } else {
                log.error("Cannot update product because of product is null");
                throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED);
            }
        } catch (final ApiException exception) {
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED, exception);
        } catch (final Exception exception) {
            log.error("Unknown product update error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED, exception);
        }
    }

}
