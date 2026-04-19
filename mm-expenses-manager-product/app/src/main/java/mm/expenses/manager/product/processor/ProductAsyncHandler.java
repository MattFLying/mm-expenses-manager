package mm.expenses.manager.product.processor;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.async.AsyncMessageProducer;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.product.price.ProductPrice;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductAsyncHandler {

    private final AsyncMessageProducer producer;
    private final ProductMapper mapper;

    public void sendProductMessage(final Product product, final AsyncKafkaOperation operation) {
        producer.send(mapper.map(product, operation));
    }

    public void sendProductPricesMessages(final Product product, final List<ProductPrice> prices, final AsyncKafkaOperation operation) {
        prices.forEach(price -> {
            producer.send(mapper.mapProductPrice(product, price, operation));
        });
    }

}
