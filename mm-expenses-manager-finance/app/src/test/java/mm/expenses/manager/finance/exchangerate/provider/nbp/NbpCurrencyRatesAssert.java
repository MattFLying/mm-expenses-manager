package mm.expenses.manager.finance.exchangerate.provider.nbp;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import org.assertj.core.api.AbstractAssert;

import java.time.LocalDate;
import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyRateAssert.assertNbpCurrencyRate;
import static org.assertj.core.api.Assertions.assertThat;

public class NbpCurrencyRatesAssert extends AbstractAssert<NbpCurrencyRatesAssert, Collection<NbpCurrencyRate>> {

    public NbpCurrencyRatesAssert(final Collection<NbpCurrencyRate> actual) {
        super(actual, NbpCurrencyRatesAssert.class);
    }

    public static NbpCurrencyRatesAssert assertNbpCurrencyRates(final Collection<NbpCurrencyRate> actual) {
        return new NbpCurrencyRatesAssert(actual);
    }

    public NbpCurrencyRatesAssert isEmpty() {
        assertThat(actual).isNotNull().isEmpty();
        return this;
    }

    public NbpCurrencyRatesAssert areOfCurrency(final CurrencyCode currency) {
        for (var rate : actual) {
            assertThat(rate.getCurrency()).isEqualTo(currency);
        }
        return this;
    }

    public NbpCurrencyRatesAssert containsExactlyTheSameObjectsAs(final Collection<NbpCurrencyRate> expected) {
        assertThat(actual).hasSameSizeAs(expected);
        assertThat(actual).hasSameElementsAs(expected);
        return this;
    }

    public NbpCurrencyRatesAssert doesNotContainAnyOf(final NbpCurrencyRate... expected) {
        assertThat(actual).doesNotContain(expected);
        return this;
    }

    public NbpCurrencyRatesAssert hasDetails() {
        for (var rate : actual) {
            assertThat(rate.getDetails(NbpCurrencyRate.Details.TABLE_TYPE.getProperty())).isPresent();
            assertThat(rate.getDetails(NbpCurrencyRate.Details.TABLE_NUMBER.getProperty())).isPresent();
        }
        return this;
    }

    public NbpCurrencyRatesAssert forCurrencyHasElements(final CurrencyCode currency, final long count) {
        final var currentCount = actual.stream().filter(rate -> rate.getCurrency().equals(currency)).count();
        assertThat(currentCount).isEqualTo(count);
        return this;
    }

    public NbpCurrencyRatesAssert allForCurrencySameAsWithoutDate(final CurrencyCode currency, final NbpCurrencyRate expected) {
        actual.stream().filter(rate -> rate.getCurrency().equals(currency)).forEach(rate -> {
            assertNbpCurrencyRate(rate)
                    .isOfCurrency(expected.getCurrency())
                    .hasDetails(expected.getDetails());
            assertThat(rate.getTableType()).isEqualTo(expected.getTableType());
            assertThat(rate.getTableNumber()).isEqualTo(expected.getTableNumber());
        });
        return this;
    }

    public NbpCurrencyRatesAssert allForCurrencyInDateRange(final CurrencyCode currency, final LocalDate dateFrom, final LocalDate dateTo) {
        final var allDates = Stream.iterate(
                dateFrom,
                date -> date.isBefore(dateTo),
                date -> date.plusDays(1)
        ).collect(Collectors.toCollection(TreeSet::new));

        final var actualDates = actual.stream().filter(rate -> rate.getCurrency().equals(currency)).map(CurrencyRate::getDate).collect(Collectors.toSet());
        assertThat(actualDates).hasSameSizeAs(allDates);
        assertThat(actualDates).containsExactlyInAnyOrderElementsOf(actualDates);

        return this;
    }

}
