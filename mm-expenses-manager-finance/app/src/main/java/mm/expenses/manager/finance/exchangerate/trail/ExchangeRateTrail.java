package mm.expenses.manager.finance.exchangerate.trail;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation.State;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Collection;

@Data
@EqualsAndHashCode
@Builder(toBuilder = true)
@Document(collection = "exchange-rates-trails")
@CompoundIndexes({
        @CompoundIndex(name = "state_idx", def = "{'state' : 1}"),
        @CompoundIndex(name = "state_date_idx", def = "{'state' : 1, 'date': 1}"),
        @CompoundIndex(name = "operation_date_idx", def = "{'operation' : 1, 'date': 1}"),
        @CompoundIndex(name = "operation_state_idx", def = "{'operation' : 1, 'state': 1}"),
        @CompoundIndex(name = "operation_state_date_idx", def = "{'operation' : 1, 'state': 1, 'date': 1}")
})
public class ExchangeRateTrail {

    @Id
    private final String id;

    private final TrailOperation operation;

    private final State state;

    private final Instant date;

    private final Long evaluated;

    private final Long skipped;

    private final Collection<String> affectedIds;

}
