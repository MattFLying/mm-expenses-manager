package mm.expenses.manager.order.product;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.order.currency.Price;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder(toBuilder = true)
@Document(collection = "products")
class ProductEntity {

    @Id
    private final String id;

    private final String name;

    private final Price price;

    private final Instant createdAt;

    private final Instant lastModifiedAt;

    @Version
    private final Long version;

}
