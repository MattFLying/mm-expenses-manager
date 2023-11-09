package mm.expenses.manager.finance.cache.exchangerate;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import org.assertj.core.api.AbstractAssert;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ExchangeRateCacheAssert extends AbstractAssert<ExchangeRateCacheAssert, ExchangeRateCache> {

    public ExchangeRateCacheAssert(final ExchangeRateCache actual) {
        super(actual, ExchangeRateCacheAssert.class);
    }

    public static ExchangeRateCacheAssert assertExchangeRateCache(final Optional<ExchangeRateCache> actual) {
        assertThat(actual).isNotEmpty();
        return assertExchangeRateCache(actual.get());
    }

    public static ExchangeRateCacheAssert assertExchangeRateCache(final ExchangeRateCache actual) {
        return new ExchangeRateCacheAssert(actual);
    }

    public ExchangeRateCacheAssert hasId(final String id) {
        assertThat(actual.getId()).isEqualTo(id);
        return this;
    }

    public ExchangeRateCacheAssert isOfCurrency(final CurrencyCode currency) {
        assertThat(actual.getCurrency()).isEqualTo(currency);
        return this;
    }

    public ExchangeRateCacheAssert isOfDate(final LocalDate today) {
        assertThat(actual.getDate()).isEqualTo(today);
        return this;
    }

    public ExchangeRateCacheAssert hasFrom(final ExchangeRate.Rate rate) {
        assertThat(actual.getFrom().getCurrency()).isEqualTo(rate.getFrom().getCurrency());
        assertThat(actual.getFrom().getRate()).isEqualTo(rate.getFrom().getValue());
        return this;
    }

    public ExchangeRateCacheAssert hasFrom(final ExchangeRateCache.RateCache from) {
        assertThat(actual.getFrom()).isEqualTo(from);
        return this;
    }

    public ExchangeRateCacheAssert hasTo(final ExchangeRate.Rate rate) {
        assertThat(actual.getTo().getCurrency()).isEqualTo(rate.getTo().getCurrency());
        assertThat(actual.getTo().getRate()).isEqualTo(rate.getTo().getValue());
        return this;
    }

    public ExchangeRateCacheAssert hasTo(final ExchangeRateCache.RateCache to) {
        assertThat(actual.getTo()).isEqualTo(to);
        return this;
    }

    public ExchangeRateCacheAssert isLatest() {
        assertThat(actual.isLatest()).isTrue();
        return this;
    }

    public ExchangeRateCacheAssert isNotLatest() {
        assertThat(actual.isLatest()).isFalse();
        return this;
    }

}
