package mm.expenses.manager.product.processor.delete.many;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiInternalErrorException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductAsyncHandler;
import mm.expenses.manager.product.core.ProductRepository;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Step in chain of multiple {@link Product}s deletion.
 * Save deleted products.
 */
@Slf4j
@RequiredArgsConstructor
final class SaveDeletedProducts extends DeleteMultipleProductsChain<Set<UUID>> {

    private final ProductRepository repository;
    private final ProductAsyncHandler asyncHandler;

    @Override
    public List<Product> handleRequest(final Set<UUID> ids) {
        try {
            context.getArgument(FindProductsToRemove.PRODUCTS_TO_REMOVE_KEY)
                    .ifPresentOrElse(
                            productsToRemove -> {
                                final var saved = repository.saveAll(((List<Product>) productsToRemove));
                                context.addArgument(Context.SAVED_KEY, saved);
                                context.addArgument(FindProductsToRemove.PRODUCTS_TO_REMOVE_KEY, (List<Product>) productsToRemove);
                            },
                            () -> {
                                log.error("Cannot delete multiple products because of one of required parameters is null.");
                                throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_DELETED);
                            }
                    );

            if (!hasNext()) {
                setNextHandler(new AsyncProductsDelete(asyncHandler));
            }
            return next.handleRequest(ids);
        } catch (final IllegalArgumentException exception) {
            log.error("Product entities or one of them are null.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_DELETED, exception);
        } catch (final OptimisticLockingFailureException exception) {
            log.error("Product entities have optimistic lock failure.", exception);
            throw new ApiInternalErrorException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_DELETED, exception);
        } catch (final ApiException exception) {
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_DELETED, exception);
        } catch (final Exception exception) {
            log.error("Unknown multiple products deletion error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_DELETED, exception);
        }
    }

}
