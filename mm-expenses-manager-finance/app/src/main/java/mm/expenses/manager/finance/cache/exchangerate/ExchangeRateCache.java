package mm.expenses.manager.finance.cache.exchangerate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.ExchangeRate.CurrencyValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@Builder(toBuilder = true)
@RedisHash("exchange-rates")
public class ExchangeRateCache implements Serializable {

    @Id
    @Indexed
    private final String id;

    @Indexed
    private final CurrencyCode currency;

    private final RateCache from;

    private final RateCache to;

    @Indexed
    private final LocalDate date;

    @Indexed
    private final boolean isLatest;

    public ExchangeRateCache disableLatest() {
        return this.toBuilder().isLatest(false).build();
    }

    public static ExchangeRateCache of(final ExchangeRate exchangeRate, final String provider) {
        return ExchangeRateCache.of(exchangeRate, false, provider);
    }

    public static ExchangeRateCache of(final ExchangeRate exchangeRate, final boolean isLatest, final String provider) {
        return ExchangeRateCache.builder()
                .id(exchangeRate.getId())
                .currency(exchangeRate.getCurrency())
                .date(DateUtils.instantToLocalDateUTC(exchangeRate.getDate()))
                .from(RateCache.of(exchangeRate.getRateByProvider(provider).getFrom()))
                .to(RateCache.of(exchangeRate.getRateByProvider(provider).getTo()))
                .isLatest(isLatest)
                .build();
    }

    public static ExchangeRateCache empty(final CurrencyCode code) {
        return ExchangeRateCache.builder()
                .currency(code)
                .from(RateCache.of(code, 0.0))
                .to(RateCache.of(code, 0.0))
                .isLatest(false)
                .build();
    }

    @Data
    @EqualsAndHashCode
    @Builder(toBuilder = true)
    public static class RateCache implements Serializable {

        private final CurrencyCode currency;

        private final Double rate;

        public static RateCache of(final CurrencyValue currencyValue) {
            return RateCache.of(currencyValue.getCurrency(), currencyValue.getValue());
        }

        public static RateCache of(final CurrencyCode currency, final Double rate) {
            return RateCache.builder().currency(currency).rate(rate).build();
        }

    }

}
