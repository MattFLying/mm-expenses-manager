package mm.expenses.manager.finance.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Schema(name = "ExchangeRatesDto", description = "Exchange rate response for specific currency code.")
@Getter
@JsonPropertyOrder({"currency", "rates"})
public class ExchangeRatesDto {

    @Schema(description = "Currency code.")
    private final String currency;

    @Schema(description = "Exchange rates for specific currency code.")
    private final Collection<ExchangeRateDto> rates;

    @Generated
    public ExchangeRatesDto(final String currency, final Collection<ExchangeRateDto> rates) {
        this.currency = currency;
        this.rates = Objects.nonNull(rates) ? rates : new ArrayList<>();
    }

    @Schema(name = "ExchangeRateDto", description = "Exchange rate response for specific currency code and date.")
    @Getter
    @RequiredArgsConstructor
    @JsonPropertyOrder({"date", "rate"})
    public static class ExchangeRateDto {

        @Schema(description = "Exchange rate for specific currency date.")
        private final LocalDate date;

        @Schema(description = "Exchange rate.")
        private final RateDto rate;

        @Schema(name = "RateDto", description = "Rate response for exchange rate.")
        @Getter
        @Builder
        @RequiredArgsConstructor
        @JsonPropertyOrder({"from", "to"})
        public static class RateDto {

            @Schema(description = "Exchange rate from currency.")
            private final CurrencyValueDto from;

            @Schema(description = "Exchange rate to currency.")
            private final CurrencyValueDto to;

        }

        @Schema(name = "CurrencyValueDto", description = "Exchange rate value.")
        @Getter
        @Builder
        @RequiredArgsConstructor
        @JsonPropertyOrder({"currency", "value"})
        public static class CurrencyValueDto {

            @Schema(description = "Exchange rate currency code.")
            private final String currency;

            @Schema(description = "Exchange rate value.")
            private final Double value;

        }

    }

}
