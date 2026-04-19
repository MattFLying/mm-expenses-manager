package mm.expenses.manager.product.processor.delete.single;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiInternalErrorException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductAsyncHandler;
import mm.expenses.manager.product.processor.ProductRepository;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.UUID;

/**
 * Step in chain of single {@link Product} deletion.
 * Saves deleted product.
 */
@Slf4j
@RequiredArgsConstructor
final class SaveDeletedProduct extends DeleteProductByIdChain {

    private final ProductRepository repository;
    private final ProductAsyncHandler asyncHandler;

    @Override
    public Product handleRequest(final UUID productId) {
        try {
            context.getArgument(FindProductToRemove.PRODUCT_KEY)
                    .ifPresentOrElse(
                            product -> {
                                final var saved = repository.save((Product) product);
                                context.addArgument(Context.SAVED_KEY, saved);
                                context.addArgument(FindProductToRemove.PRODUCT_KEY, saved);
                            },
                            () -> {
                                log.error("Cannot delete single product because of one of required parameters is null.");
                                throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_DELETED);
                            }
                    );

            if (!hasNext()) {
                setNextHandler(new AsyncProductDelete(asyncHandler));
            }
            return next.handleRequest(productId);
        } catch (final IllegalArgumentException exception) {
            log.error("Product entity is null.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_DELETED, exception);
        } catch (final OptimisticLockingFailureException exception) {
            log.error("Product entity has optimistic lock failure.", exception);
            throw new ApiInternalErrorException(ProductExceptionMessage.PRODUCT_CANNOT_BE_DELETED, exception);
        } catch (final ApiException exception) {
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_DELETED, exception);
        } catch (final Exception exception) {
            log.error("Unknown single product deletion error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_DELETED, exception);
        }
    }

}
