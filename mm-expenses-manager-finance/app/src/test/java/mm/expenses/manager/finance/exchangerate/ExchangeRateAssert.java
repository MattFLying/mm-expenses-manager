package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import org.assertj.core.api.AbstractAssert;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ExchangeRateAssert extends AbstractAssert<ExchangeRateAssert, ExchangeRate> {

    public ExchangeRateAssert(final ExchangeRate actual) {
        super(actual, ExchangeRateAssert.class);
    }

    public static ExchangeRateAssert assertExchangeRate(final Optional<ExchangeRate> actual) {
        assertThat(actual).isNotEmpty();
        return assertExchangeRate(actual.get());
    }

    public static ExchangeRateAssert assertExchangeRate(final ExchangeRate actual) {
        return new ExchangeRateAssert(actual);
    }

    public ExchangeRateAssert hasId(final String id) {
        assertThat(actual.getId()).isEqualTo(id);
        return this;
    }

    public ExchangeRateAssert isOfCurrency(final CurrencyCode currency) {
        assertThat(actual.getCurrency()).isEqualTo(currency);
        return this;
    }

    public ExchangeRateAssert isOfDate(final Instant today) {
        assertThat(actual.getDate()).isEqualTo(today);
        return this;
    }

    public ExchangeRateAssert createdAt(final Instant createdAt) {
        assertThat(actual.getCreatedAt()).isEqualTo(createdAt);
        return this;
    }

    public ExchangeRateAssert modifiedAt(final Instant modifiedAt) {
        assertThat(actual.getModifiedAt()).isEqualTo(modifiedAt);
        return this;
    }

    public ExchangeRateAssert hasVersion(final Long version) {
        assertThat(actual.getVersion()).isEqualTo(version);
        return this;
    }

    public ExchangeRateAssert hasRates(final Map<String, ExchangeRate.Rate> rates) {
        assertThat(actual.getRatesByProvider()).isEqualTo(rates);
        return this;
    }

    public ExchangeRateAssert hasDetails(final Map<String, Map<String, Object>> details) {
        assertThat(actual.getDetailsByProvider()).isEqualTo(details);
        return this;
    }

    public ExchangeRateAssert hasRateForProvider(final String provider, final ExchangeRate.Rate rate) {
        assertThat(actual.getRatesByProvider()).isNotNull().isNotEmpty().containsKey(provider).containsEntry(provider, rate);
        assertThat(actual.getRateByProvider(provider)).isEqualTo(rate);

        final var actualRate = actual.getRatesByProvider().get(provider);
        assertThat(actualRate.getFrom()).isEqualTo(rate.getFrom());
        assertThat(actualRate.getTo()).isEqualTo(rate.getTo());

        return this;
    }

    public ExchangeRateAssert hasDetailsForProvider(final String provider, final Map<String, Object> details) {
        assertThat(actual.getDetailsByProvider()).isNotNull().isNotEmpty().containsKey(provider).containsEntry(provider, details);

        return this;
    }

}
