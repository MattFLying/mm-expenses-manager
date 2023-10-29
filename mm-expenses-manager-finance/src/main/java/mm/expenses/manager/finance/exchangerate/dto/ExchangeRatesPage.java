package mm.expenses.manager.finance.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;

@Schema(name = "ExchangeRatesPage", description = "Paged simple exchange rates response.")
@RequiredArgsConstructor
@JsonPropertyOrder({"content", "currencies"})
public class ExchangeRatesPage {

    @NotNull
    private final Collection<ExchangeRatesDto> content;

    @Schema(description = "Exchange rates for each currency code.")
    public Collection<ExchangeRatesDto> getContent() {
        return content;
    }

    @Schema(description = "Retrieved currency codes count.")
    public int getCurrencies() {
        return content.size();
    }

}
