package mm.expenses.manager.finance.nbp.model;

import lombok.*;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.financial.CurrencyRate;

import java.time.LocalDate;

@Getter
public class NbpCurrencyRate extends CurrencyRate {

    private final NbpDetails nbpDetails;

    @Builder
    public NbpCurrencyRate(final CurrencyCode currency, final LocalDate date, final Double rate, final NbpDetails nbpDetails) {
        super(currency, date, rate);
        this.nbpDetails = nbpDetails;
    }

    @Data
    @Builder(toBuilder = true)
    public static class NbpDetails {

        private final TableType tableType;

        private final String tableNumber;

    }

}
