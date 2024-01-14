package mm.expenses.manager.order.product.model;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.order.async.message.ProductManagementConsumerMessage;
import mm.expenses.manager.order.currency.Price;

import java.time.Instant;
import java.util.Objects;

@Data
@Builder(toBuilder = true)
public class Product {

    private final String id;

    private final String name;

    private final Price price;

    private final Instant createdAt;

    private final Instant lastModifiedAt;

    private final Long version;

    public static Product of(final ProductManagementConsumerMessage message) {
        final var priceMessage = message.getPrice();
        final var price = Objects.nonNull(priceMessage) ? Price.of(priceMessage) : null;

        return Product.builder()
                .id(message.getId().toString())
                .name(message.getName())
                .lastModifiedAt(message.getLastModifiedAt())
                .price(price)
                .build();
    }

}
