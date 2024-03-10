package mm.expenses.manager.product.async.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.kafka.producer.AsyncKafkaProducerBinding;
import mm.expenses.manager.product.product.Product;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProductManagementProducerMessage implements AsyncKafkaProducerBinding, Serializable {

    private UUID id;

    private String name;

    private PriceMessage price;

    private Map<String, Object> details;

    private Boolean isDeleted;

    private Instant createdAt;

    private Instant lastModifiedAt;

    private AsyncKafkaOperation operation;

    @Override
    public String getProducerBindingName() {
        return "productManagement-out-0";
    }

    @Override
    public String getProducerTopicName() {
        return "product-management";
    }

    public static ProductManagementProducerMessage of(final Product product, final AsyncKafkaOperation operation) {
        return ProductManagementProducerMessage.builder()
                .id(product.getId())
                .name(product.getName())
                .price(PriceMessage.of(product.getPrice()))
                .details(product.getDetails())
                .isDeleted(product.isDeleted())
                .createdAt(product.getCreatedAt())
                .lastModifiedAt(product.getLastModifiedAt())
                .operation(operation)
                .build();
    }

}
