package mm.expenses.manager.finance.nbp.model;

import lombok.*;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.common.CurrencyProviderType;
import mm.expenses.manager.finance.financial.CurrencyRate;
import mm.expenses.manager.finance.financial.CurrencyRateProvider.CurrencyDetails;

import java.beans.ConstructorProperties;
import java.time.LocalDate;

@Getter
@EqualsAndHashCode(callSuper = true)
public class NbpCurrencyRate extends CurrencyRate {

    @Builder
    public NbpCurrencyRate(final CurrencyCode currency, final LocalDate date, final Double rate, final NbpDetails nbpDetails) {
        super(currency, date, rate, nbpDetails);
    }

    @Data
    @Builder(toBuilder = true)
    public static class NbpDetails implements CurrencyDetails {

        private final TableType tableType;

        private final String tableNumber;

        @ConstructorProperties({"tableType", "tableNumber"})
        public NbpDetails(final TableType tableType, final String tableNumber) {
            this.tableType = tableType;
            this.tableNumber = tableNumber;
        }

        @Override
        public CurrencyProviderType getType() {
            return CurrencyProviderType.NBP;
        }

    }

}
