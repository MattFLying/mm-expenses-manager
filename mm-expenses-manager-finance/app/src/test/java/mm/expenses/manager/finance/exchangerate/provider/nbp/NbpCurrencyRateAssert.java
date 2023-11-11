package mm.expenses.manager.finance.exchangerate.provider.nbp;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import org.assertj.core.api.AbstractAssert;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class NbpCurrencyRateAssert extends AbstractAssert<NbpCurrencyRateAssert, NbpCurrencyRate> {

    public NbpCurrencyRateAssert(final NbpCurrencyRate actual) {
        super(actual, NbpCurrencyRateAssert.class);
    }

    public static NbpCurrencyRateAssert assertNbpCurrencyRate(final Optional<NbpCurrencyRate> actual) {
        assertThat(actual).isNotEmpty();
        return assertNbpCurrencyRate(actual.get());
    }

    public static NbpCurrencyRateAssert assertNbpCurrencyRate(final NbpCurrencyRate actual) {
        return new NbpCurrencyRateAssert(actual);
    }

    public NbpCurrencyRateAssert hasDate(final LocalDate date) {
        assertThat(actual.getDate()).isEqualTo(date);
        return this;
    }

    public NbpCurrencyRateAssert hasRate(final Double rate) {
        assertThat(actual.getRate()).isEqualTo(rate);
        return this;
    }

    public NbpCurrencyRateAssert isOfCurrency(final CurrencyCode currency) {
        assertThat(actual.getCurrency()).isEqualTo(currency);
        return this;
    }

    public NbpCurrencyRateAssert hasDetails(final Map<String, Object> details) {
        assertThat(actual.getDetails()).isNotNull().containsExactlyInAnyOrderEntriesOf(details);
        assertThat(actual.getDetails(NbpCurrencyRate.Details.TABLE_TYPE.getProperty())).isPresent();
        assertThat(actual.getDetails(NbpCurrencyRate.Details.TABLE_NUMBER.getProperty())).isPresent();
        return this;
    }

}
