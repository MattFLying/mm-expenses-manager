package mm.expenses.manager.product.repository;

import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;
import mm.expenses.manager.common.beans.async.AsyncMessageProducer;
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
    private static AsyncMessageProducer producer;

    public static ProductRepository productRepository() {
        return productRepository;
    }

    public static AsyncMessageProducer asyncProducer() {
        return producer;
    }

    @Bean
    static Object initRepositoryRegistry(final ProductRepository productRepo, final AsyncMessageProducer asyncProducer) {
        productRepository = productRepo;
        producer = asyncProducer;
        return null;
    }

}
