package mm.expenses.manager.order.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Objects;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductPrices extends LinkedList<ProductPrice> implements Serializable {

    public boolean exists(final CurrencyCode currency) {
        return stream().anyMatch(price -> Objects.equals(price.getCurrency(), currency));
    }

    public ProductPrice get(final CurrencyCode currency) {
        return stream().filter(price -> Objects.equals(price.getCurrency(), currency)).findFirst().orElse(null);
    }

    public void update(final CurrencyCode currency, final BigDecimal value, final String date, final Boolean isOriginal) {
        final var price = get(currency);
        if (Objects.nonNull(price)) {
            price.update(value, date, isOriginal);
        }
    }

}
