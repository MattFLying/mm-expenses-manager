package mm.expenses.manager.order.async.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PriceMessage {

    private CurrencyCode currency;

    private BigDecimal value;

}
