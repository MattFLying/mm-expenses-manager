package mm.expenses.manager.product.processor.delete.many;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductAsyncHandler;
import mm.expenses.manager.product.processor.ProductRepository;
import mm.expenses.manager.product.processor.delete.single.DeleteSingleProduct;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Step in chain of multiple {@link Product}s deletion.
 * Delete products one-by-one.
 */
@Slf4j
@RequiredArgsConstructor
final class DeleteMultipleProducts extends DeleteMultipleProductsChain<Set<UUID>> {

    private final ProductRepository repository;
    private final ProductAsyncHandler asyncHandler;
    private final Instant deletionTime;

    @Override
    public List<Product> handleRequest(final Set<UUID> ids) {
        try {
            context.getArgument(FindProductsToRemove.PRODUCTS_TO_REMOVE_KEY)
                    .ifPresentOrElse(
                            productsToRemove -> {
                                ((List<Product>) productsToRemove).forEach(product -> DeleteSingleProduct.delete(product, deletionTime));

                                context.addArgument(FindProductsToRemove.PRODUCTS_TO_REMOVE_KEY, productsToRemove);
                            },
                            () -> {
                                log.error("Cannot delete multiple products because of one of required parameters is null.");
                                throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_DELETED);
                            }
                    );

            if (!hasNext()) {
                setNextHandler(new SaveDeletedProducts(repository, asyncHandler));
            }
            return next.handleRequest(ids);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown multiple products deletion error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_DELETED, exception);
        }
    }

}
