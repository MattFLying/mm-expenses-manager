package mm.expenses.manager.product.async.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.product.price.Price;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PriceMessage {

    private CurrencyCode currency;

    private BigDecimal value;

    public static PriceMessage of(final Price price) {
        return PriceMessage.builder()
                .value(price.getValue())
                .currency(price.getCurrency())
                .build();
    }

}
