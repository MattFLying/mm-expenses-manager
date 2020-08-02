package mm.expenses.manager.order.order.model;

import lombok.Builder;
import lombok.Data;

import java.beans.ConstructorProperties;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class CreateNewOrder {

    private final String name;

    private final List<CreateNewOrderedProduct> orderedProducts;

    @ConstructorProperties({"name", "orderedProducts"})
    public CreateNewOrder(final String name, final List<CreateNewOrderedProduct> orderedProducts) {
        this.name = name;
        this.orderedProducts = orderedProducts;
    }

    @Data
    @Builder(toBuilder = true)
    public static class CreateNewOrderedProduct {

        private final String productId;

        private final Double quantity;

        @ConstructorProperties({"productId", "quantity"})
        public CreateNewOrderedProduct(final String productId, final Double quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

    }

}
