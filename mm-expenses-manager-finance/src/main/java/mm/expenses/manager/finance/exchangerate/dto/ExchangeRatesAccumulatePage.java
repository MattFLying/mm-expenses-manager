package mm.expenses.manager.finance.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;

@Schema(name = "ExchangeRatesAccumulatePage", description = "Exchange rates response with accumulated pages per currency code.")
@RequiredArgsConstructor
@JsonPropertyOrder({"content", "currencies", "exchangeRates", "totalExchangeRates"})
public class ExchangeRatesAccumulatePage {

    @NotNull
    private final Collection<ExchangeRatePage> content;

    @Schema(description = "Exchange rates for each currency code.")
    public Collection<ExchangeRatePage> getContent() {
        return content;
    }

    @Schema(description = "Retrieved currency codes count.")
    public int getCurrencies() {
        return content.size();
    }

    @Schema(description = "Retrieved exchange rates count.")
    public int getExchangeRates() {
        return content.stream().mapToInt(page -> page.getContent().getRates().size()).sum();
    }

    @Schema(description = "Total available exchange rates.")
    public long getTotalExchangeRates() {
        return content.stream().mapToLong(ExchangeRatePage::getTotalElements).sum();
    }

    @Schema(name = "ExchangeRatePage", description = "Exchange rates response for specific currency code.")
    @RequiredArgsConstructor
    @JsonPropertyOrder({"content", "numberOfElements", "totalElements", "totalPages", "sortBy", "hasNext", "isFirst", "isLast"})
    public static class ExchangeRatePage {

        @NotNull
        private final ExchangeRatesDto content;

        @NotNull
        private final Page<?> ratesPage;

        @Schema(description = "Exchange rates for specific currency code.")
        public ExchangeRatesDto getContent() {
            return content;
        }

        @Schema(description = "Retrieved exchange rates count.")
        public Integer getNumberOfElements() {
            return ratesPage.getNumberOfElements();
        }

        @Schema(description = "Total available exchange rates for currency.")
        public Long getTotalElements() {
            return ratesPage.getTotalElements();
        }

        @Schema(description = "Total available pages of exchange rates for currency.")
        public Integer getTotalPages() {
            return ratesPage.getTotalPages();
        }

        @Schema(description = "Defines if there is more available pages.")
        public Boolean getHasNext() {
            return ratesPage.hasNext();
        }

        @Schema(description = "Defines if retrieved page is first.")
        public Boolean getIsFirst() {
            return ratesPage.isFirst();
        }

        @Schema(description = "Defines if retrieved page is last.")
        public Boolean getIsLast() {
            return ratesPage.isLast();
        }

    }

}
