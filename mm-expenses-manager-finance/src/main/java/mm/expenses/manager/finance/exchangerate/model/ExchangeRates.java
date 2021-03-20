package mm.expenses.manager.finance.exchangerate.model;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.time.LocalDate;
import java.util.*;

@Data
public class ExchangeRates {

    private final CurrencyCode currency;

    private final Collection<ExchangeRate> rates;

    @Builder(toBuilder = true)
    public ExchangeRates(final CurrencyCode currency, final Collection<ExchangeRate> rates) {
        this.currency = currency;
        this.rates = Objects.nonNull(rates) ? rates : new ArrayList<>();
    }

    @Data
    public static class ExchangeRate {

        private final LocalDate date;

        private final Map<String, Rate> ratesByProvider;

        private final Map<String, Map<String, Object>> detailsByProvider;

        @Builder(toBuilder = true)
        public ExchangeRate(final LocalDate date, final Map<String, Rate> ratesByProvider, final Map<String, Map<String, Object>> detailsByProvider) {
            this.date = date;
            this.ratesByProvider = Objects.nonNull(ratesByProvider) ? ratesByProvider : new HashMap<>();
            this.detailsByProvider = Objects.nonNull(detailsByProvider) ? detailsByProvider : new HashMap<>();
        }

        public Rate getRateByProvider(final String providerName) {
            return ratesByProvider.getOrDefault(providerName, null);
        }

        @Data
        @Builder(toBuilder = true)
        public static class Rate {

            private final CurrencyValue from;
            private final CurrencyValue to;

        }

        @Data
        @Builder(toBuilder = true)
        public static class CurrencyValue {

            private final CurrencyCode currency;
            private final Double value;

        }

    }

}
