package mm.expenses.manager.finance.exchangerate.model;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.time.LocalDate;
import java.util.Collection;

@Data
@Builder(toBuilder = true)
public class CurrencyRates {

    private final CurrencyCode currency;

    private final Collection<CurrencyRate> rates;

    @Data
    @Builder(toBuilder = true)
    public static class CurrencyRate {

        private final LocalDate date;

        private final Double rate;

    }

}
