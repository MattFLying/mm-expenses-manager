package mm.expenses.manager.product.processor.delete.single;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductAsyncHandler;
import mm.expenses.manager.product.core.ProductRepository;

import java.time.Instant;
import java.util.UUID;

/**
 * Step in chain of single {@link Product} deletion.
 * Deletes product and its associated models with specific flags.
 */
@Slf4j
@RequiredArgsConstructor
public final class DeleteSingleProduct extends DeleteProductByIdChain {

    private final ProductRepository repository;
    private final ProductAsyncHandler asyncHandler;
    private final Instant deletionTime;

    @Override
    public Product handleRequest(final UUID productId) {
        try {
            context.getArgument(FindProductToRemove.PRODUCT_KEY)
                    .ifPresentOrElse(
                            product -> {
                                delete((Product) product, deletionTime);

                                context.addArgument(FindProductToRemove.PRODUCT_KEY, (Product) product);
                            },
                            () -> {
                                log.error("Cannot delete single product because of one of required parameters is null.");
                                throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_DELETED);
                            }
                    );

            if (!hasNext()) {
                setNextHandler(new SaveDeletedProduct(repository, asyncHandler));
            }
            return next.handleRequest(productId);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown single product deletion error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_DELETED, exception);
        }
    }

    public static void delete(final Product product, final Instant deletionTime) {
        product.setDeleted(true);
        product.setLastModifiedAt(deletionTime);

        product.getPrices().forEach(price -> {
            price.setDeleted(true);
            price.setLastModifiedAt(deletionTime);
        });
    }

}
