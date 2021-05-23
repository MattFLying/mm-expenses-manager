package mm.expenses.manager.product.product.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import mm.expenses.manager.product.price.Price;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class CreateProductCommand {

    private String name;

    private Price price;

    private Map<String, Object> details;

}
