package mm.expenses.manager.finance.nbp.model;

import lombok.*;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.common.CurrencyProviderType;
import mm.expenses.manager.finance.financial.CurrencyRate;
import mm.expenses.manager.finance.financial.CurrencyRateProvider.CurrencyDetails;

import java.beans.ConstructorProperties;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@EqualsAndHashCode(callSuper = true)
public class NbpCurrencyRate extends CurrencyRate {

    @Builder
    public NbpCurrencyRate(final CurrencyCode currency, final LocalDate date, final NbpDetails nbpDetails) {
        super(currency, date, nbpDetails);
    }

    @Data
    @Builder(toBuilder = true)
    public static class NbpDetails implements CurrencyDetails {

        private final TableType tableType;

        private final String tableNumber;

        private final Double rate;

        @ConstructorProperties({"tableType", "tableNumber", "rate"})
        public NbpDetails(final TableType tableType, final String tableNumber, final Double rate) {
            this.tableType = tableType;
            this.tableNumber = tableNumber;
            this.rate = rate;
        }

        @Override
        public CurrencyProviderType getType() {
            return CurrencyProviderType.NBP;
        }

        public Double getRate() {
            return Objects.nonNull(rate) ? rate : 0.0;
        }

    }

}
