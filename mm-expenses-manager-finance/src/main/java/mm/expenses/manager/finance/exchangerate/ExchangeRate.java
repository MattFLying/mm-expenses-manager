package mm.expenses.manager.finance.exchangerate;

import lombok.*;
import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@RequiredArgsConstructor
@Document(collection = "exchange-rates")
@EqualsAndHashCode
@CompoundIndexes({
        @CompoundIndex(name = "date_idx", def = "{'date': 1}"),
        @CompoundIndex(name = "currency_idx", def = "{'currency': 1}"),
        @CompoundIndex(name = "currency_date_idx", def = "{'currency' : 1, 'date': 1}", unique = true)
})
class ExchangeRate {

    static final String DEFAULT_SORT_BY = "date";

    @Id
    private final String id;

    private final CurrencyCode currency;

    private final Instant date;

    private final Instant createdAt;

    private final Instant modifiedAt;

    private Map<String, Rate> ratesByProvider;

    private Map<String, Map<String, Object>> detailsByProvider;

    @Version
    private final Long version;

    void addRateForProvider(final String providerName, final Rate rate) {
        if (Objects.isNull(ratesByProvider)) {
            ratesByProvider = new HashMap<>();
        }
        ratesByProvider.put(providerName, rate);
    }

    void addDetailsForProvider(final String providerName, final Map<String, Object> details) {
        if (Objects.isNull(detailsByProvider)) {
            detailsByProvider = new HashMap<>();
        }
        detailsByProvider.put(providerName, details);
    }

    Rate getRateByProvider(final String providerName) {
        return getRateByProvider(providerName, false);
    }

    Rate getRateByProvider(final String providerName, final boolean findAnyIfNotExist) {
        if (Objects.isNull(ratesByProvider)) {
            ratesByProvider = new HashMap<>();
        }
        if (!findAnyIfNotExist) {
            return ratesByProvider.getOrDefault(providerName, Rate.empty());
        }
        return Optional.ofNullable(ratesByProvider.get(providerName))
                .orElseGet(() -> ratesByProvider.values()
                        .stream()
                        .findAny()
                        .orElse(Rate.empty()));
    }

    boolean hasProvider(final String providerName) {
        if (Objects.isNull(ratesByProvider)) {
            ratesByProvider = new HashMap<>();
        }
        if (Objects.isNull(detailsByProvider)) {
            detailsByProvider = new HashMap<>();
        }
        return ratesByProvider.containsKey(providerName) && detailsByProvider.containsKey(providerName);
    }

    static ExchangeRate modified(final ExchangeRate modified, final Instant modifiedDate) {
        return modified.toBuilder().modifiedAt(modifiedDate).build();
    }

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    @Builder(toBuilder = true)
    static class Rate {

        private final CurrencyValue from;
        private final CurrencyValue to;

        static Rate empty() {
            return ExchangeRate.Rate.builder()
                    .from(ExchangeRate.CurrencyValue.empty())
                    .to(ExchangeRate.CurrencyValue.empty())
                    .build();
        }

        static Rate of(final CurrencyCode currencyFrom, final CurrencyCode currencyTo, final Double currencyValueTo) {
            return ExchangeRate.Rate.builder()
                    .from(ExchangeRate.CurrencyValue.of(currencyFrom))
                    .to(ExchangeRate.CurrencyValue.of(currencyTo, currencyValueTo))
                    .build();
        }

    }

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    @Builder(toBuilder = true)
    static class CurrencyValue {

        private static final Double UNKNOWN_CURRENCY_VALUE = 0.0;
        private static final Double INITIAL_CURRENCY_VALUE = 1.0;

        private final CurrencyCode currency;
        private final Double value;

        static CurrencyValue empty() {
            return ExchangeRate.CurrencyValue.builder().currency(CurrencyCode.UNDEFINED).value(UNKNOWN_CURRENCY_VALUE).build();
        }

        static CurrencyValue of(final CurrencyCode currency) {
            return of(currency, INITIAL_CURRENCY_VALUE);
        }

        static CurrencyValue of(final CurrencyCode currency, final Double value) {
            return ExchangeRate.CurrencyValue.builder().currency(currency).value(value).build();
        }

    }

}
