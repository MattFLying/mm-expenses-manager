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

    public static ProductRepository productRepository() {
        return productRepository;
    }

    @Bean
    static Object initRepositoryRegistry(final ProductRepository productRepo) {
        productRepository = productRepo;
        return null;
    }

}
