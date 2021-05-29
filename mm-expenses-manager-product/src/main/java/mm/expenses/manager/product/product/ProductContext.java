package mm.expenses.manager.product.product;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import mm.expenses.manager.product.product.query.ProductQueryFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    public static Optional<Product> findProductById(final String id) {
        return productRepository().findById(id);
    }

    public static Page<Product> findAll(final ProductQueryFilter queryFilter, final Pageable pageable) {
        final var filter = queryFilter.findFilter();
        switch (filter) {
            case NAME:
                return productRepository().findByName(queryFilter.getName(), pageable);
            case PRICE:
                return productRepository().findByPrice_value(queryFilter.getPrice(), pageable);
            case NAME_PRICE:
                return productRepository().findByNameAndPrice_value(queryFilter.getName(), queryFilter.getPrice(), pageable);
            case NAME_PRICE_LESS_THAN:
                return productRepository().findByNameAndPrice_valueLessThan(queryFilter.getName(), queryFilter.getPrice(), pageable);
            case NAME_PRICE_GREATER_THAN:
                return productRepository().findByNameAndPrice_valueGreaterThan(queryFilter.getName(), queryFilter.getPrice(), pageable);
            case NAME_PRICE_MIN_PRICE_MAX:
                return productRepository().findByNameAndPrice_valueBetween(queryFilter.getName(), queryFilter.getPriceMin(), queryFilter.getPriceMax(), pageable);
            case PRICE_LESS_THAN:
                return productRepository().findByPrice_valueLessThan(queryFilter.getPrice(), pageable);
            case PRICE_GREATER_THAN:
                return productRepository().findByPrice_valueGreaterThan(queryFilter.getPrice(), pageable);
            case PRICE_MIN_PRICE_MAX:
                return productRepository().findByPrice_valueBetween(queryFilter.getPriceMin(), queryFilter.getPriceMax(), pageable);
            default:
                return productRepository().findAll(pageable);
        }
    }

}
