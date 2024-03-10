package mm.expenses.manager.order.async.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.kafka.consumer.AsyncKafkaConsumerBinding;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProductManagementConsumerMessage implements AsyncKafkaConsumerBinding, Serializable {

    private UUID id;

    private String name;

    private PriceMessage price;

    private Map<String, Object> details;

    private Boolean isDeleted;

    private Instant createdAt;

    private Instant lastModifiedAt;

    private AsyncKafkaOperation operation;

    @Override
    public String getConsumerBindingName() {
        return "productManagement-in-0";
    }

    @Override
    public String getConsumerTopicName() {
        return "product-management";
    }

}
