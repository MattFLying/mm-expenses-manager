package mm.expenses.manager.finance.financial;

import lombok.AllArgsConstructor;
import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.financial.CurrencyRateProvider.CurrencyDetails;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Abstract representation of the currency rate with default properties described the currency rate
 */
@Data
@AllArgsConstructor
public abstract class CurrencyRate {

    private final CurrencyCode currency;

    private final LocalDate date;

    private final CurrencyDetails details;

    public Optional<CurrencyDetails> getDetails() {
        return Optional.ofNullable(details);
    }

}
