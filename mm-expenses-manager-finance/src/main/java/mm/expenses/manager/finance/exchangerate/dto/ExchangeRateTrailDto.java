package mm.expenses.manager.finance.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation;

import java.time.Instant;
import java.util.Collection;

@Schema(name = "ExchangeRateTrailDto", description = "Exchange rate trail response.")
@Getter
@Builder
@RequiredArgsConstructor
@JsonPropertyOrder({"operation", "state", "date", "evaluated", "skipped", "affectedIds"})
public class ExchangeRateTrailDto {

    @Schema(description = "Operation type.")
    private final TrailOperation operation;

    @Schema(description = "Operation status.")
    private final TrailOperation.State state;

    @Schema(description = "Date when operation occurred.")
    private final Instant date;

    @Schema(description = "Defines how many exchange rates has been evaluated.")
    private final Long evaluated;

    @Schema(description = "Defines how many exchange rates has been skipped.")
    private final Long skipped;

    @Schema(description = "Ids of exchange rates that have been under evaluation.")
    private final Collection<String> affectedIds;

}
