package mm.expenses.manager.product.product;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import mm.expenses.manager.product.product.query.ProductQueryFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;

import static mm.expenses.manager.product.repository.RepositoryRegistry.productRepository;

/**
 * Product context to process any operation on products.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductContext {

    public static Product saveProduct(final Product product) {
        return productRepository().save(product);
    }

    public static Optional<Product> findProductById(final String productId) {
        return productRepository().findById(productId);
    }

    public static void markAsDeleted(final Product product) {
        productRepository().save(product);
    }

    public static void removeProducts(final Collection<String> productIds) {
        productRepository().deleteByIdIn(productIds);
    }

    public static Page<Product> findDeleted(final Pageable pageable) {
        return productRepository().findAllByIsDeletedTrue(pageable);
    }

    public static Page<Product> findAll(final ProductQueryFilter queryFilter, final Pageable pageable) {
        final var filter = queryFilter.findFilter();
        switch (filter) {
            case NAME:
                return productRepository().findByName(queryFilter.getName(), pageable);
            case PRICE:
                return productRepository().findByPrice_valueAndIsDeletedFalse(queryFilter.getPrice(), pageable);
            case NAME_PRICE:
                return productRepository().findByNameAndPrice_value(queryFilter.getName(), queryFilter.getPrice(), pageable);
            case NAME_PRICE_LESS_THAN:
                return productRepository().findByNameAndPrice_valueLessThan(queryFilter.getName(), queryFilter.getPrice(), pageable);
            case NAME_PRICE_GREATER_THAN:
                return productRepository().findByNameAndPrice_valueGreaterThan(queryFilter.getName(), queryFilter.getPrice(), pageable);
            case NAME_PRICE_MIN_PRICE_MAX:
                return productRepository().findByNameAndPrice_valueBetween(queryFilter.getName(), queryFilter.getPriceMin(), queryFilter.getPriceMax(), pageable);
            case PRICE_LESS_THAN:
                return productRepository().findByPrice_valueLessThanAndIsDeletedFalse(queryFilter.getPrice(), pageable);
            case PRICE_GREATER_THAN:
                return productRepository().findByPrice_valueGreaterThanAndIsDeletedFalse(queryFilter.getPrice(), pageable);
            case PRICE_MIN_PRICE_MAX:
                return productRepository().findByPrice_valueBetweenAndIsDeletedFalse(queryFilter.getPriceMin(), queryFilter.getPriceMax(), pageable);
            default:
                return productRepository().findAllByIsDeletedFalse(pageable);
        }
    }

}
