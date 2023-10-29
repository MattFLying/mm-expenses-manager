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
        switch (filter) {
            case NAME:
                return RepositoryRegistry.productRepository().findByName(queryFilter.getName(), pageable);
            case PRICE:
                return RepositoryRegistry.productRepository().findByPrice_valueAndIsDeletedFalse(queryFilter.getPrice(), pageable);
            case NAME_PRICE:
                return RepositoryRegistry.productRepository().findByNameAndPrice_value(queryFilter.getName(), queryFilter.getPrice(), pageable);
            case NAME_PRICE_LESS_THAN:
                return RepositoryRegistry.productRepository().findByNameAndPrice_valueLessThan(queryFilter.getName(), queryFilter.getPrice(), pageable);
            case NAME_PRICE_GREATER_THAN:
                return RepositoryRegistry.productRepository().findByNameAndPrice_valueGreaterThan(queryFilter.getName(), queryFilter.getPrice(), pageable);
            case NAME_PRICE_MIN_PRICE_MAX:
                return RepositoryRegistry.productRepository().findByNameAndPrice_valueBetween(queryFilter.getName(), queryFilter.getPriceMin(), queryFilter.getPriceMax(), pageable);
            case PRICE_LESS_THAN:
                return RepositoryRegistry.productRepository().findByPrice_valueLessThanAndIsDeletedFalse(queryFilter.getPrice(), pageable);
            case PRICE_GREATER_THAN:
                return RepositoryRegistry.productRepository().findByPrice_valueGreaterThanAndIsDeletedFalse(queryFilter.getPrice(), pageable);
            case PRICE_MIN_PRICE_MAX:
                return RepositoryRegistry.productRepository().findByPrice_valueBetweenAndIsDeletedFalse(queryFilter.getPriceMin(), queryFilter.getPriceMax(), pageable);
            default:
                return RepositoryRegistry.productRepository().findAllByIsDeletedFalse(pageable);
        }
    }

}
