package mm.expenses.manager.order.order.model;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.order.currency.PriceDto;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class OrderDto {

    private final String id;

    private final String name;

    private final List<OrderedProductDto> orderedProducts;

    private final PriceDto priceSummary;

    @Data
    @Builder(toBuilder = true)
    public static class OrderedProductDto {

        private final String id;

        private final BoughtProductDto boughtProduct;

        private final Double quantity;

        private final PriceDto priceSummary;

        @Data
        @Builder(toBuilder = true)
        public static class BoughtProductDto {

            private final String productId;

            private final String name;

            private final PriceDto price;

        }

    }
}
