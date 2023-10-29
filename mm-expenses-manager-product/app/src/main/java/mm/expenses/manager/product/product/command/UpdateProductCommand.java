package mm.expenses.manager.product.product.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import mm.expenses.manager.product.price.Price;

import java.util.Map;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class UpdateProductCommand {

    private String id;

    private String name;

    private Price price;

    private Map<String, Object> details;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<Price> getPrice() {
        return Optional.ofNullable(price);
    }

    public Optional<Map<String, Object>> getDetails() {
        return Optional.ofNullable(details);
    }

}
