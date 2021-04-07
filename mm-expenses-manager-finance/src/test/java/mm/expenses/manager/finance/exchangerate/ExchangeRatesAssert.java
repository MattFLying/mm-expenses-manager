package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import org.assertj.core.api.AbstractAssert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mm.expenses.manager.finance.exchangerate.ExchangeRateAssert.assertExchangeRate;
import static org.assertj.core.api.Assertions.assertThat;

public class ExchangeRatesAssert extends AbstractAssert<ExchangeRatesAssert, Collection<ExchangeRate>> {

    public ExchangeRatesAssert(final Collection<ExchangeRate> actual) {
        super(actual, ExchangeRatesAssert.class);
    }

    public static ExchangeRatesAssert assertExchangeRates(final Stream<Page<ExchangeRate>> actualStream) {
        final var actual = actualStream.map(Slice::getContent).flatMap(Collection::stream).collect(Collectors.toList());
        return assertExchangeRates(actual);
    }

    public static ExchangeRatesAssert assertExchangeRates(final Page<ExchangeRate> actual) {
        assertThat(actual).isNotNull().isNotEmpty();
        return assertExchangeRates(actual.getContent());
    }

    public static ExchangeRatesAssert assertExchangeRates(final Collection<ExchangeRate> actual) {
        return new ExchangeRatesAssert(actual);
    }

    public ExchangeRatesAssert containsExactlyTheSameObjectsAs(final Collection<ExchangeRate> expected) {
        assertThat(actual).hasSameSizeAs(expected);
        assertThat(actual).hasSameElementsAs(expected);
        return this;
    }

    public ExchangeRatesAssert forCurrencyHasExactlyTheSameAs(final CurrencyCode currency, final ExchangeRate expectedRate) {
        final var actualRate = actual.stream().filter(rate -> rate.getId().equals(expectedRate.getId())).findAny();
        assertExchangeRate(actualRate)
                .hasId(expectedRate.getId())
                .isOfCurrency(currency)
                .isOfDate(expectedRate.getDate())
                .createdAt(expectedRate.getCreatedAt())
                .modifiedAt(expectedRate.getModifiedAt())
                .hasRates(expectedRate.getRatesByProvider())
                .hasDetails(expectedRate.getDetailsByProvider())
                .hasVersion(ExchangeRateHelper.INITIAL_VERSION);

        return this;
    }

    public ExchangeRatesAssert forCurrencyHasExactlyTheSameAs(final CurrencyCode currency, final Collection<ExchangeRate> expectedRates) {
        final var actualRates = actual.stream().filter(rate -> rate.getCurrency().equals(currency)).collect(Collectors.toMap(ExchangeRate::getDate, Function.identity()));
        final var expectedMap = expectedRates.stream().collect(Collectors.toMap(ExchangeRate::getDate, Function.identity()));

        actualRates.forEach((date, actualRate) -> {
            assertThat(expectedMap).containsKey(date);
            final var expected = expectedMap.get(date);
            assertExchangeRate(actualRate)
                    .hasId(expected.getId())
                    .isOfCurrency(currency)
                    .isOfDate(expected.getDate())
                    .createdAt(expected.getCreatedAt())
                    .modifiedAt(expected.getModifiedAt())
                    .hasRates(expected.getRatesByProvider())
                    .hasDetails(expected.getDetailsByProvider())
                    .hasVersion(ExchangeRateHelper.INITIAL_VERSION);
        });
        return this;
    }

}
