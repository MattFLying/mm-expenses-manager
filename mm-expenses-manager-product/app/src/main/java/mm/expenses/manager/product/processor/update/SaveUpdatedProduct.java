package mm.expenses.manager.product.processor.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductAsyncHandler;
import mm.expenses.manager.product.core.ProductRepository;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Instant;

/**
 * Step in chain of {@link Product} update.
 * Builds the updated {@link Product} object and save it into database.
 */
@Slf4j
@RequiredArgsConstructor
final class SaveUpdatedProduct extends UpdateProductChain {

    private final ProductRepository repository;
    private final ProductAsyncHandler asyncHandler;
    private final Instant modificationTime;

    @Override
    public Product handleRequest(final UpdateProductRequest request) {
        try {
            context.getArgument(FindProductToUpdate.PRODUCT_KEY)
                    .ifPresentOrElse(
                            product -> {
                                var isUpdated = (boolean) context.getArgument(UpdateProductData.IS_UPDATED_KEY).orElse(false);

                                if (isUpdated) {
                                    final var toSave = (Product) product;
                                    toSave.setLastModifiedAt(modificationTime);

                                    final var saved = repository.save(toSave);
                                    context.addArgument(Context.SAVED_KEY, saved);
                                    context.addArgument(FindProductToUpdate.PRODUCT_KEY, saved);
                                } else {
                                    context.addArgument(FindProductToUpdate.PRODUCT_KEY, product);
                                }
                            },
                            () -> {
                                log.error("Cannot update product because of one of required parameters is null.");
                                throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED);
                            }
                    );

            if (!hasNext()) {
                setNextHandler(new AsyncProductUpdate(asyncHandler));
            }
            return next.handleRequest(request);
        } catch (final IllegalArgumentException exception) {
            log.error("Product entity is null.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED, exception);
        } catch (final OptimisticLockingFailureException exception) {
            log.error("Product entity has optimistic lock failure.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED, exception);
        } catch (final ApiException exception) {
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED, exception);
        } catch (final Exception exception) {
            log.error("Unknown product update error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED, exception);
        }
    }

}
