package mm.expenses.manager.finance.currency.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@Schema(name = "CurrencyDto", description = "Currency codes response.")
@Getter
@RequiredArgsConstructor
@JsonPropertyOrder({"codes", "currenciesCount"})
public class CurrencyDto {

    @Schema(description = "Available currency codes.")
    private final Collection<String> codes;

    @Schema(description = "Available currency codes count.")
    public int getCurrenciesCount() {
        return codes.size();
    }

}
