package mm.expenses.manager.finance.cache.exchangerate;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import org.assertj.core.api.AbstractAssert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheAssert.assertExchangeRateCache;
import static org.assertj.core.api.Assertions.assertThat;

public class ExchangeRatesCacheAssert extends AbstractAssert<ExchangeRatesCacheAssert, Collection<ExchangeRateCache>> {

    public ExchangeRatesCacheAssert(final Collection<ExchangeRateCache> actual) {
        super(actual, ExchangeRatesCacheAssert.class);
    }

    public static ExchangeRatesCacheAssert assertExchangeRatesCache(final Stream<Page<ExchangeRateCache>> actualStream) {
        final var actual = actualStream.map(Slice::getContent).flatMap(Collection::stream).collect(Collectors.toList());
        return assertExchangeRatesCache(actual);
    }

    public static ExchangeRatesCacheAssert assertExchangeRatesCache(final Page<ExchangeRateCache> actual) {
        assertThat(actual).isNotNull().isNotEmpty();
        return assertExchangeRatesCache(actual.getContent());
    }

    public static ExchangeRatesCacheAssert assertExchangeRatesCache(final Collection<ExchangeRateCache> actual) {
        return new ExchangeRatesCacheAssert(actual);
    }

    public ExchangeRatesCacheAssert forCurrencyHasExactlyTheSameAsAndIsLatest(final CurrencyCode currency, final ExchangeRate expected) {
        final var actualRate = actual.stream().filter(rate -> rate.getId().equals(expected.getId())).findAny();
        assertThat(actualRate).isNotEmpty();

        final var result = actualRate.get();
        assertExchangeRateCache(actualRate)
                .hasId(result.getId())
                .isOfCurrency(currency)
                .isOfDate(result.getDate())
                .hasFrom(result.getFrom())
                .hasTo(result.getTo())
                .isLatest();

        return this;
    }

    public ExchangeRatesCacheAssert forCurrencyHasExactlyTheSameAsAndIsNotLatest(final CurrencyCode currency, final ExchangeRate expected) {
        final var actualRate = actual.stream().filter(rate -> rate.getId().equals(expected.getId())).findAny();
        assertThat(actualRate).isNotEmpty();

        final var result = actualRate.get();
        assertExchangeRateCache(actualRate)
                .hasId(result.getId())
                .isOfCurrency(currency)
                .isOfDate(result.getDate())
                .hasFrom(result.getFrom())
                .hasTo(result.getTo())
                .isNotLatest();

        return this;
    }

    public ExchangeRatesCacheAssert isEmpty() {
        assertThat(actual).isEmpty();

        return this;
    }

}
