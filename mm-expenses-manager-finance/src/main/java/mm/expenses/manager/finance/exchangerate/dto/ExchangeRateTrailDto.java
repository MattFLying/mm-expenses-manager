package mm.expenses.manager.finance.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation;

import java.time.Instant;
import java.util.Collection;

@Getter
@Builder
@RequiredArgsConstructor
@JsonPropertyOrder({"operation", "state", "date", "evaluated", "skipped", "affectedIds"})
public class ExchangeRateTrailDto {

    private final TrailOperation operation;

    private final TrailOperation.State state;

    private final Instant date;

    private final Long evaluated;

    private final Long skipped;

    private final Collection<String> affectedIds;

}
