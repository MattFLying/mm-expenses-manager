package mm.expenses.manager.product.processor.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductAsyncHandler;

/**
 * Step in chain of new {@link Product} creation.
 * Process asynchronous communication when new product is created and stored in database.
 */
@Slf4j
@RequiredArgsConstructor
final class AsyncProductCreation extends CreateProductChain {

    private final ProductAsyncHandler asyncHandler;

    @Override
    public void setNextHandler(final ChainCommandExecution<CreateProductRequest, Product> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public Product handleRequest(final CreateProductRequest request) {
        try {
            final var savedOpt = context.getArgument(ChainCommandExecution.Context.SAVED_KEY);
            if (savedOpt.isPresent()) {
                final var saved = (Product) savedOpt.get();
                asyncHandler.sendProductMessage(saved, AsyncKafkaOperation.CREATE);

                if (hasNext()) {
                    // This step should not ever happen but if there is any possibility to do anything after save in database
                    // then there is a proper place to do it.
                    return next.handleRequest(request);
                } else {
                    return saved;
                }
            } else {
                log.error("Cannot create product because of product is null.");
                throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_CREATED);
            }
        } catch (final ApiException exception) {
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_CREATED, exception);
        } catch (final Exception exception) {
            log.error("Unknown product creation error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_CREATED, exception);
        }
    }

}
