package mm.expenses.manager.finance.exchangerate.trail;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public class ExchangeRateTrailHelper {

    public static final String ID = UUID.randomUUID().toString();

    public static ExchangeRateTrail createNewExchangeRateTrail(final TrailOperation operation, final Instant date, final long evaluatedCount,
                                                               final long skippedCount, final Collection<String> affectedIds) {
        return ExchangeRateTrail.builder()
                .id(ID)
                .operation(operation)
                .state(operation.getState())
                .date(date)
                .evaluated(evaluatedCount)
                .skipped(skippedCount)
                .affectedIds(affectedIds)
                .build();
    }

}
