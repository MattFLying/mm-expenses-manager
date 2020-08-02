package mm.expenses.manager.order.order.model;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.order.currency.Price;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Data
@Builder(toBuilder = true)
public class Order {

    private final String id;

    private final String name;

    private final List<OrderedProduct> orderedProducts;

    private final Price priceSummary;

    private final Instant createdAt;

    private final Instant lastModifiedAt;

    public static Price calculatePriceSummary(final List<OrderedProduct> products) {
        return CollectionUtils.isEmpty(products)
                ? Price.empty()
                : products.stream().findFirst()
                .map(firstForCurrency -> {
                    final var currency = firstForCurrency.getPriceSummary().getCurrency();
                    return products.stream()
                            .map(OrderedProduct::getPriceSummary)
                            .reduce(new Price(currency, BigDecimal.ZERO), Order::accumulatePrices);
                })
                .orElse(Price.empty());
    }

    private static Price accumulatePrices(final Price first, final Price second) {
        return Price.add(first, second);
    }

    @Data
    @Builder(toBuilder = true)
    public static class OrderedProduct {

        private final String id;

        private final BoughtProduct boughtProduct;

        private final Double quantity;

        private final Instant createdAt;

        private final Instant lastModifiedAt;

        public Price getPriceSummary() {
            return Objects.nonNull(boughtProduct)
                    ? Price.multiply(boughtProduct.getPrice(), quantity)
                    : Price.empty();
        }

        public static OrderedProduct mergeForSameProduct(final OrderedProduct first, final OrderedProduct second) {
            return OrderedProduct.builder()
                    .id(first.id)
                    .createdAt(first.createdAt)
                    .lastModifiedAt(second.lastModifiedAt)
                    .quantity(first.quantity + second.quantity)
                    .boughtProduct(first.boughtProduct)
                    .build();
        }

        @Data
        @Builder(toBuilder = true)
        public static class BoughtProduct {

            private final String productId;

            private final String name;

            private final Price price;

        }

    }

}
