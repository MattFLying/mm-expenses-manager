package mm.expenses.manager.order.order;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.order.currency.Price;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Document(collection = "orders")
class OrderEntity {

    @Id
    private final String id;

    private final Instant createdAt;

    private final Instant lastModifiedAt;

    private final String name;

    private final List<OrderedProductEntity> orderedProducts;

    private final Price priceSummary;

    @Data
    @Builder(toBuilder = true)
    static class OrderedProductEntity {

        private final String id;

        private final Instant createdAt;

        private final Instant lastModifiedAt;

        private final Double quantity;

        private final BoughtProduct boughtProduct;

        private final Price priceSummary;

        @Data
        @Builder(toBuilder = true)
        static class BoughtProduct {

            private final String productId;

            private final String name;

            private final Price price;

        }

    }

}
