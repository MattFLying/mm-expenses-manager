package mm.expenses.manager.finance.exchangerate.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Getter
@JsonPropertyOrder({"currency", "rates"})
public class ExchangeRatesDto {

    private final CurrencyCode currency;

    private final Collection<ExchangeRateDto> rates;

    public ExchangeRatesDto(final CurrencyCode currency, final Collection<ExchangeRateDto> rates) {
        this.currency = currency;
        this.rates = Objects.nonNull(rates) ? rates : new ArrayList<>();
    }

    @Getter
    @RequiredArgsConstructor
    @JsonPropertyOrder({"date", "rate"})
    public static class ExchangeRateDto {

        private final LocalDate date;

        private final RateDto rate;

        @Getter
        @RequiredArgsConstructor
        @JsonPropertyOrder({"from", "to"})
        public static class RateDto {

            private final CurrencyValueDto from;
            private final CurrencyValueDto to;

        }

        @Getter
        @RequiredArgsConstructor
        @JsonPropertyOrder({"currency", "value"})
        public static class CurrencyValueDto {

            private final CurrencyCode currency;
            private final Double value;

        }

    }

}
