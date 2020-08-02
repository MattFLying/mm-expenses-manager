package mm.expenses.manager.order.product.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.order.currency.PriceDto;

@Getter
@RequiredArgsConstructor
@Builder(toBuilder = true)
public class ProductDto {

    private final String id;

    private final String name;

    private final PriceDto price;

}
