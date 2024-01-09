package mm.expenses.manager.product.repository;

import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;
import mm.expenses.manager.product.async.AsyncMessageSender;
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
    private static AsyncMessageSender producer;

    public static ProductRepository productRepository() {
        return productRepository;
    }

    public static AsyncMessageSender asyncProducer() {
        return producer;
    }

    @Bean
    static Object initRepositoryRegistry(final ProductRepository productRepo, final AsyncMessageSender asyncProducer) {
        productRepository = productRepo;
        producer = asyncProducer;
        return null;
    }

}
