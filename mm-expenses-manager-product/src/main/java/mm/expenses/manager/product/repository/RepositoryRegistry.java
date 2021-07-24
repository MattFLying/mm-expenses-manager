package mm.expenses.manager.product.repository;

import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Repository registry to register all available repositories.
 */
@Generated
@Configuration
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class RepositoryRegistry {

    private static ProductRepository productRepository;

    private static CategoryRepository categoryRepository;

    public static ProductRepository productRepository() {
        return productRepository;
    }

    public static CategoryRepository categoryRepository() {
        return categoryRepository;
    }

    @Bean
    static Object initRepositoryRegistry(final ProductRepository productRepo,
                                         final CategoryRepository categoryRepo) {
        productRepository = productRepo;
        categoryRepository = categoryRepo;
        return null;
    }

}
