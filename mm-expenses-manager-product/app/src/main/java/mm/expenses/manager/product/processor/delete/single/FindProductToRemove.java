package mm.expenses.manager.product.processor.delete.single;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductAsyncHandler;
import mm.expenses.manager.product.processor.ProductRepository;

import java.time.Instant;
import java.util.UUID;

/**
 * Step in chain of single {@link Product} deletion.
 * Finds requested product to be removed.
 */
@Slf4j
@RequiredArgsConstructor
final class FindProductToRemove extends DeleteProductByIdChain {

    final static String PRODUCT_KEY = "product";

    private final ProductRepository repository;
    private final ProductAsyncHandler asyncHandler;
    private final Instant deletionTime;

    @Override
    public Product handleRequest(final UUID productId) {
        try {
            repository.findByIdAndIsDeleted(productId, false)
                    .ifPresentOrElse(
                            product -> context.addArgument(PRODUCT_KEY, product),
                            () -> {
                                throw new ApiNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId));
                            }
                    );

            if (!hasNext()) {
                setNextHandler(new DeleteSingleProduct(repository, asyncHandler, deletionTime));
            }
            return next.handleRequest(productId);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown single product deletion error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_DELETED, exception);
        }
    }

}
