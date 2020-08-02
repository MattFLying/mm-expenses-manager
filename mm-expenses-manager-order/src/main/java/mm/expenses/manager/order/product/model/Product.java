package mm.expenses.manager.order.product.model;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.order.currency.Price;

import java.time.Instant;

@Data
@Builder(toBuilder = true)
public class Product {

    private final String id;

    private final String name;

    private final Price price;

    private final Instant createdAt;

    private final Instant lastModifiedAt;

    private final Long version;

}
