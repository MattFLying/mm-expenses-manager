package mm.expenses.manager.order.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.order.async.message.ProductManagementConsumerMessage;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    public List<Product> findAllByIds(final Set<UUID> ids) {
        log.info("Looking for products of given ids: {}", ids);
        final var products = repository.findByIdIn(ids);
        log.info("Found: {} products", products.size());
        return products;
    }

    public void createProductFromKafkaTopic(final ProductManagementConsumerMessage message) {
        final var productId = message.getId();
        repository.findById(productId).ifPresentOrElse(product -> {
                    log.info("Received CREATE {} message for product id {} that already exists. Body: {}", message.getConsumerBindingName(), message.getId(), message);
                },
                () -> {
                    log.info("Received CREATE {} message. Body: {}", message.getConsumerBindingName(), message);
                    var product = repository.save(mapper.mapCreate(message));
                    log.info("Product created: {}", product);
                });
    }

    public void updateProductFromKafkaTopic(final ProductManagementConsumerMessage message) {
        final var productId = message.getId();
        repository.findById(productId).ifPresentOrElse(product -> {
                    log.info("Received UPDATE {} message. Body: {}", message.getConsumerBindingName(), message);
                    product = repository.save(mapper.mapUpdate(product, message));
                    log.info("Product updated: {}", product);
                },
                () -> {
                    log.info("Received UPDATE {} message for product id {} that does not exists. Body: {}", message.getConsumerBindingName(), message.getId(), message);
                });
    }

    public void deleteProductFromKafkaTopic(final ProductManagementConsumerMessage message) {
        final var productId = message.getId();
        repository.findById(productId).ifPresentOrElse(product -> {
                    log.info("Received DELETE {} message. Body: {}", message.getConsumerBindingName(), message);
                    product.setDeleted(message.getIsDeleted());
                    product.setLastModifiedAt(message.getLastModifiedAt());
                    product = repository.save(product);
                    log.info("Product deleted: {}", product);
                },
                () -> {
                    log.info("Received DELETE {} message for product id {} that does not exists. Body: {}", message.getConsumerBindingName(), message.getId(), message);
                });
    }

}
