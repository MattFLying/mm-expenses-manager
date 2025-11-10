package mm.expenses.manager.common.kafka.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;

import java.math.BigDecimal;

/**
 * Common representation of price async message.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PriceMessage {

    private CurrencyCode currency;

    private BigDecimal value;

    private Boolean isOriginal;

    private String date;

}
