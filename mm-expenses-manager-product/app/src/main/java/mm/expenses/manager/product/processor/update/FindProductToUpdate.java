package mm.expenses.manager.product.processor.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductAsyncHandler;
import mm.expenses.manager.product.processor.ProductRepository;

import java.time.Instant;
import java.util.UUID;

/**
 * Step in chain of {@link Product} update.
 * Finds product of given id.
 */
@Slf4j
@RequiredArgsConstructor
final class FindProductToUpdate extends UpdateProductChain {

    final static String PRODUCT_KEY = "product";

    private final ProductRepository repository;
    private final ProductAsyncHandler asyncHandler;
    private final Instant modificationTime;

    @Override
    public Product handleRequest(final UpdateProductRequest request) {
        try {
            context.getArgument(ValidateRequestedProductUpdates.PRODUCT_ID_KEY)
                    .ifPresentOrElse(
                            productId -> {
                                final var existingProduct = repository.findByIdAndIsDeleted((UUID) productId, false)
                                        .orElseThrow(() -> new ApiNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId)));

                                context.addArgument(PRODUCT_KEY, existingProduct);
                            },
                            () -> {
                                log.error("Cannot update product because of one of required parameters is null.");
                                throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED);
                            }
                    );

            if (!hasNext()) {
                setNextHandler(new UpdateProductData(repository, asyncHandler, modificationTime));
            }
            return next.handleRequest(request);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown product update error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED, exception);
        }
    }

}
