package mm.expenses.manager.order.product.model;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.order.currency.PriceDto;

import java.beans.ConstructorProperties;

@Data
@Builder(toBuilder = true)
public class UpdateProduct {

    private final String name;

    private final PriceDto price;

    @ConstructorProperties({"name", "price"})
    public UpdateProduct(final String name, final PriceDto price) {
        this.name = name;
        this.price = price;
    }

}
