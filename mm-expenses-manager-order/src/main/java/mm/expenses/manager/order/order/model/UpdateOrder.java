package mm.expenses.manager.order.order.model;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.order.order.model.CreateNewOrder.CreateNewOrderedProduct;

import java.beans.ConstructorProperties;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class UpdateOrder {

    private final String name;

    private final List<UpdateOrderedProduct> orderedProducts;
    private final List<CreateNewOrderedProduct> newProducts;

    @ConstructorProperties({"name", "orderedProducts", "newProducts"})
    public UpdateOrder(final String name, final List<UpdateOrderedProduct> orderedProducts, final List<CreateNewOrderedProduct> newProducts) {
        this.name = name;
        this.orderedProducts = orderedProducts;
        this.newProducts = newProducts;
    }

    @Data
    @Builder(toBuilder = true)
    public static class UpdateOrderedProduct {

        private final String id;

        private final Double quantity;

        @ConstructorProperties({"id", "quantity"})
        public UpdateOrderedProduct(final String id, final Double quantity) {
            this.id = id;
            this.quantity = quantity;
        }

    }
}
