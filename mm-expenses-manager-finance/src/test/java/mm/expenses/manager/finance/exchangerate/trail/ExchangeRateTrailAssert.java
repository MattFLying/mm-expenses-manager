package mm.expenses.manager.finance.exchangerate.trail;

import mm.expenses.manager.finance.exchangerate.trail.TrailOperation.State;
import org.assertj.core.api.AbstractAssert;

import java.time.Instant;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class ExchangeRateTrailAssert extends AbstractAssert<ExchangeRateTrailAssert, ExchangeRateTrail> {

    public ExchangeRateTrailAssert(final ExchangeRateTrail actual) {
        super(actual, ExchangeRateTrailAssert.class);
    }

    public static ExchangeRateTrailAssert assertExchangeRateTrail(final ExchangeRateTrail actual) {
        assertThat(actual).isNotNull();
        return new ExchangeRateTrailAssert(actual);
    }

    public ExchangeRateTrailAssert hasId(final String id) {
        assertThat(actual.getId()).isEqualTo(id);
        return this;
    }

    public ExchangeRateTrailAssert isOfOperation(final TrailOperation operation) {
        assertThat(actual.getOperation()).isEqualTo(operation);
        return this;
    }

    public ExchangeRateTrailAssert hasState(final State state) {
        assertThat(actual.getState()).isEqualTo(state);
        return this;
    }

    public ExchangeRateTrailAssert ofDate(final Instant date) {
        assertThat(actual.getDate()).isEqualTo(date);
        return this;
    }

    public ExchangeRateTrailAssert hasEvaluated(final long evaluatedCount) {
        assertThat(actual.getEvaluated()).isEqualTo(evaluatedCount);
        return this;
    }

    public ExchangeRateTrailAssert hasSkipped(final long skippedCount) {
        assertThat(actual.getSkipped()).isEqualTo(skippedCount);
        return this;
    }

    public ExchangeRateTrailAssert hasAffectedIds(final Collection<String> affectedIds) {
        assertThat(actual.getAffectedIds()).isNotNull().containsExactlyInAnyOrderElementsOf(affectedIds);
        return this;
    }

}
