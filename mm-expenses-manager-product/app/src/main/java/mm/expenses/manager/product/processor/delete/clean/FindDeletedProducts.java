package mm.expenses.manager.product.processor.delete.clean;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Step in chain of already marked as deleted {@link Product}s to be permanently deleted.
 * Find all products marked as deleted.
 */
@Slf4j
@RequiredArgsConstructor
final class FindDeletedProducts extends HardDeleteProductsChain<Pageable, Page<Product>> {

    private final ProductRepository repository;

    @Getter
    private Collection<UUID> productIds;

    @Override
    public Page<Product> handleRequest(final Pageable pageable) {
        try {
            final var markedAsDeletedPage = repository.findAllByIsDeletedTrue(pageable);
            productIds = markedAsDeletedPage.getContent()
                    .stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            if (hasNext()) {
                return next.handleRequest(pageable);
            }
            return markedAsDeletedPage;
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown products hard deletion error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_PERMANENTLY_DELETED, exception);
        }
    }

}
