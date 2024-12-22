package mm.expenses.manager.finance.exchangerate;

import lombok.*;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode
@Builder(toBuilder = true)
@Document(collection = "exchange-rates")
@CompoundIndexes({
        @CompoundIndex(name = "date_idx", def = "{'date': 1}"),
        @CompoundIndex(name = "currency_idx", def = "{'currency': 1}"),
        @CompoundIndex(name = "currency_date_idx", def = "{'currency' : 1, 'date': 1}", unique = true)
})
public class ExchangeRate implements Serializable {

    @Id
    private String id;

    @Field(value="currency")
    private CurrencyCode currency;

    @Field(value="date")
    private Instant date;

    @Field(value="createdAt")
    private Instant createdAt;

    @Field(value="modifiedAt")
    private Instant modifiedAt;

    @Field(value="ratesByProvider")
    private Map<String, Rate> ratesByProvider;

    @Field(value="detailsByProvider")
    private Map<String, Map<String, Object>> detailsByProvider;

    @Version
    @Field(value="version")
    private Long version;

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

    public Rate getRateByProvider(final String providerName) {
        return getRateByProvider(providerName, false);
    }

    public Rate getRateByProvider(final String providerName, final boolean findAnyIfNotExist) {
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Rate implements Serializable {

        private CurrencyValue from;
        private CurrencyValue to;

        public static Rate empty() {
            return ExchangeRate.Rate.builder()
                    .from(ExchangeRate.CurrencyValue.empty())
                    .to(ExchangeRate.CurrencyValue.empty())
                    .build();
        }

        static Rate of(final CurrencyCode currencyFrom, final CurrencyCode currencyTo, final BigDecimal currencyValueTo) {
            return ExchangeRate.Rate.builder()
                    .from(ExchangeRate.CurrencyValue.of(currencyFrom))
                    .to(ExchangeRate.CurrencyValue.of(currencyTo, currencyValueTo))
                    .build();
        }

    }

    @Data
    @Builder
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrencyValue implements Serializable {

        private CurrencyCode currency;
        private BigDecimal value;

        public BigDecimal getValue() {
            return BigDecimalWrapper.of(value);
        }

        static CurrencyValue empty() {
            return ExchangeRate.CurrencyValue.builder().currency(CurrencyCode.UNDEFINED).value(BigDecimalWrapper.zero()).build();
        }

        static CurrencyValue of(final CurrencyCode currency) {
            return of(currency, BigDecimalWrapper.one());
        }

        static CurrencyValue of(final CurrencyCode currency, final BigDecimal value) {
            return ExchangeRate.CurrencyValue.builder().currency(currency).value(BigDecimalWrapper.of(value)).build();
        }

    }

}
