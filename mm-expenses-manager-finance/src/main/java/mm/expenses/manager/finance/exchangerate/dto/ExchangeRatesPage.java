package mm.expenses.manager.finance.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@RequiredArgsConstructor
@JsonPropertyOrder({"content", "currencies"})
public class ExchangeRatesPage {

    @NotNull
    private final Collection<ExchangeRatesDto> content;

    public Collection<ExchangeRatesDto> getContent() {
        return content;
    }

    public int getCurrencies() {
        return content.size();
    }

}
