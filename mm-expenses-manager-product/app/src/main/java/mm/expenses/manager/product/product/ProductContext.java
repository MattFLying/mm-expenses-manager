package mm.expenses.manager.product.product;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import mm.expenses.manager.product.async.message.ProductMessage;
import mm.expenses.manager.product.async.message.ProductMessage.Operation;
import mm.expenses.manager.product.product.query.ProductQueryFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import mm.expenses.manager.product.repository.RepositoryRegistry;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Product context to process any operation on products.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductContext {

    public static Product saveProduct(final Product product, final Operation operation) {
        final var savedProduct = RepositoryRegistry.productRepository().save(product);
        return sendAsyncMessage(savedProduct, operation);
    }

    public static Optional<Product> findProductById(final UUID productId) {
        return RepositoryRegistry.productRepository().findById(productId);
    }

    public static void removeProducts(final Collection<UUID> productIds) {
        RepositoryRegistry.productRepository().deleteByIdIn(productIds);
    }

    public static Page<Product> findDeleted(final Pageable pageable) {
        return RepositoryRegistry.productRepository().findAllByIsDeletedTrue(pageable);
    }

    public static Page<Product> findAll(final ProductQueryFilter queryFilter, final Pageable pageable) {
        final var filter = queryFilter.findFilter();
        return switch (filter) {
            case NAME -> RepositoryRegistry.productRepository().findByNameAndIsDeletedFalse(queryFilter.name(), pageable);
            case PRICE ->
                    RepositoryRegistry.productRepository().findByPrice_valueAndIsDeletedFalse(queryFilter.price(), pageable);
            case NAME_PRICE ->
                    RepositoryRegistry.productRepository().findByNameAndPrice_valueAndIsDeletedFalse(queryFilter.name(), queryFilter.price(), pageable);
            case NAME_PRICE_LESS_THAN ->
                    RepositoryRegistry.productRepository().findByNameAndPrice_valueLessThanAndIsDeletedFalse(queryFilter.name(), queryFilter.price(), pageable);
            case NAME_PRICE_GREATER_THAN ->
                    RepositoryRegistry.productRepository().findByNameAndPrice_valueGreaterThanAndIsDeletedFalse(queryFilter.name(), queryFilter.price(), pageable);
            case NAME_PRICE_MIN_PRICE_MAX ->
                    RepositoryRegistry.productRepository().findByNameAndPrice_valueBetweenAndIsDeletedFalse(queryFilter.name(), queryFilter.priceMin(), queryFilter.priceMax(), pageable);
            case PRICE_LESS_THAN ->
                    RepositoryRegistry.productRepository().findByPrice_valueLessThanAndIsDeletedFalse(queryFilter.price(), pageable);
            case PRICE_GREATER_THAN ->
                    RepositoryRegistry.productRepository().findByPrice_valueGreaterThanAndIsDeletedFalse(queryFilter.price(), pageable);
            case PRICE_MIN_PRICE_MAX ->
                    RepositoryRegistry.productRepository().findByPrice_valueBetweenAndIsDeletedFalse(queryFilter.priceMin(), queryFilter.priceMax(), pageable);
            default -> RepositoryRegistry.productRepository().findAllByIsDeletedFalse(pageable);
        };
    }

    private static Product sendAsyncMessage(final Product product, final Operation operation) {
        RepositoryRegistry.asyncProducer().send(ProductMessage.of(product, operation));
        return product;
    }

}
