package mm.expenses.manager.product.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.async.AsyncMessageProducer;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.product.price.ProductPrice;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
class ProductAsyncHandler {

    private final AsyncMessageProducer producer;
    private final ProductMapper mapper;

    void sendProductMessage(final Product product, final AsyncKafkaOperation operation) {
        producer.send(mapper.map(product, operation));
    }

    void sendProductPricesMessages(final Product product, final List<ProductPrice> prices, final AsyncKafkaOperation operation) {
        prices.forEach(price -> {
            producer.send(mapper.mapProductPrice(product, price, operation));
        });
    }

}
