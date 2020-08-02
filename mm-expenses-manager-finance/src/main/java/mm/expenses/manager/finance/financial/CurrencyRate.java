package mm.expenses.manager.finance.financial;

import lombok.AllArgsConstructor;
import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.time.LocalDate;

/**
 * Abstract representation of the currency rate with default properties described the currency rate
 */
@Data
@AllArgsConstructor
public abstract class CurrencyRate {

    private final CurrencyCode currency;

    private final LocalDate date;

    private final Double rate;

}
