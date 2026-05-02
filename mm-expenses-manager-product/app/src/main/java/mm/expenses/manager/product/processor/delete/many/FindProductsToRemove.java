package mm.expenses.manager.product.processor.delete.many;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductAsyncHandler;
import mm.expenses.manager.product.core.ProductRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Step in chain of multiple {@link Product}s deletion.
 * Finds requested products to be removed.
 */
@Slf4j
@RequiredArgsConstructor
final class FindProductsToRemove extends DeleteMultipleProductsChain<Set<UUID>> {

    final static String PRODUCTS_TO_REMOVE_KEY = "toRemove";

    private final ProductRepository repository;
    private final ProductAsyncHandler asyncHandler;
    private final Instant deletionTime;

    @Override
    public List<Product> handleRequest(final Set<UUID> ids) {
        try {
            final var toRemove = repository.findAllByIdInAndIsDeleted(ids, false);
            if (toRemove.size() != ids.size()) {
                final var notFoundIds = toRemove.stream()
                        .map(Product::getId)
                        .filter(productId -> !ids.contains(productId))
                        .collect(Collectors.toSet());
                throw new ApiNotFoundException(ProductExceptionMessage.PRODUCTS_NOT_FOUND.withParameters(notFoundIds));
            }

            context.addArgument(PRODUCTS_TO_REMOVE_KEY, toRemove);

            if (!hasNext()) {
                setNextHandler(new DeleteMultipleProducts(repository, asyncHandler, deletionTime));
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
