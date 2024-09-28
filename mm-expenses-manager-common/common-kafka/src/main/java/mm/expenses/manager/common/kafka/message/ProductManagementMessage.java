package mm.expenses.manager.common.kafka.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.async.AsyncBinding;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Common representation of product management async message.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProductManagementMessage implements AsyncBinding {

    private UUID id;

    private String name;

    private PriceMessage price;

    private Map<String, Object> details;

    private Boolean isDeleted;

    private Instant createdAt;

    private Instant lastModifiedAt;

    private AsyncKafkaOperation operation;

    @Override
    public String producerBindingName() {
        return "productManagement-out-0";
    }

    @Override
    public String consumerBindingName() {
        return "productManagement-in-0";
    }

    @Override
    public String topicName() {
        return "product-management";
    }

}
