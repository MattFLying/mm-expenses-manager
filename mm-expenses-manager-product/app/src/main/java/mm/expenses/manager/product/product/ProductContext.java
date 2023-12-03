package mm.expenses.manager.product.product;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import mm.expenses.manager.product.product.query.ProductQueryFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import mm.expenses.manager.product.repository.RepositoryRegistry;

import java.util.Collection;
import java.util.Optional;

/**
 * Product context to process any operation on products.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductContext {

    public static Product saveProduct(final Product product) {
        return RepositoryRegistry.productRepository().save(product);
    }

    public static Optional<Product> findProductById(final String productId) {
        return RepositoryRegistry.productRepository().findById(productId);
    }

    public static void removeProducts(final Collection<String> productIds) {
        RepositoryRegistry.productRepository().deleteByIdIn(productIds);
    }

    public static Page<Product> findDeleted(final Pageable pageable) {
        return RepositoryRegistry.productRepository().findAllByIsDeletedTrue(pageable);
    }

    public static Page<Product> findAll(final ProductQueryFilter queryFilter, final Pageable pageable) {
        final var filter = queryFilter.findFilter();
        return switch (filter) {
            case NAME -> RepositoryRegistry.productRepository().findByName(queryFilter.name(), pageable);
            case PRICE ->
                    RepositoryRegistry.productRepository().findByPrice_valueAndIsDeletedFalse(queryFilter.price(), pageable);
            case NAME_PRICE ->
                    RepositoryRegistry.productRepository().findByNameAndPrice_value(queryFilter.name(), queryFilter.price(), pageable);
            case NAME_PRICE_LESS_THAN ->
                    RepositoryRegistry.productRepository().findByNameAndPrice_valueLessThan(queryFilter.name(), queryFilter.price(), pageable);
            case NAME_PRICE_GREATER_THAN ->
                    RepositoryRegistry.productRepository().findByNameAndPrice_valueGreaterThan(queryFilter.name(), queryFilter.price(), pageable);
            case NAME_PRICE_MIN_PRICE_MAX ->
                    RepositoryRegistry.productRepository().findByNameAndPrice_valueBetween(queryFilter.name(), queryFilter.priceMin(), queryFilter.priceMax(), pageable);
            case PRICE_LESS_THAN ->
                    RepositoryRegistry.productRepository().findByPrice_valueLessThanAndIsDeletedFalse(queryFilter.price(), pageable);
            case PRICE_GREATER_THAN ->
                    RepositoryRegistry.productRepository().findByPrice_valueGreaterThanAndIsDeletedFalse(queryFilter.price(), pageable);
            case PRICE_MIN_PRICE_MAX ->
                    RepositoryRegistry.productRepository().findByPrice_valueBetweenAndIsDeletedFalse(queryFilter.priceMin(), queryFilter.priceMax(), pageable);
            default -> RepositoryRegistry.productRepository().findAllByIsDeletedFalse(pageable);
        };
    }

}
