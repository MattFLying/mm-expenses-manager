package mm.expenses.manager.product.processor.delete.clean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductRepository;

import java.util.Collection;
import java.util.UUID;

/**
 * Step in chain of already marked as deleted {@link Product}s to be permanently deleted.
 * Permanently delete products.
 */
@Slf4j
@RequiredArgsConstructor
final class HardDeleteProducts extends HardDeleteProductsChain<Collection<UUID>, Integer> {

    private final ProductRepository repository;

    @Override
    public Integer handleRequest(final Collection<UUID> productIds) {
        try {
            repository.deleteByIdIn(productIds);

            if (hasNext()) {
                return next.handleRequest(productIds);
            }
            return productIds.size();
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown products hard deletion error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_PERMANENTLY_DELETED, exception);
        }
    }

}
