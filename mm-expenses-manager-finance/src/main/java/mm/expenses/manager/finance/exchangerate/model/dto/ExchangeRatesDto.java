package mm.expenses.manager.finance.exchangerate.model.dto;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Data
public class ExchangeRatesDto {

    private final CurrencyCode currency;

    private final Collection<ExchangeRateDto> rates;

    @Builder(toBuilder = true)
    public ExchangeRatesDto(final CurrencyCode currency, final Collection<ExchangeRateDto> rates) {
        this.currency = currency;
        this.rates = Objects.nonNull(rates) ? rates : new ArrayList<>();
    }

    @Data
    public static class ExchangeRateDto {

        private final LocalDate date;

        private final Double rate;

        @Builder(toBuilder = true)
        public ExchangeRateDto(final LocalDate date, final Double rate) {
            this.date = date;
            this.rate = Objects.nonNull(rate) ? rate : 0.0;
        }

    }

}
