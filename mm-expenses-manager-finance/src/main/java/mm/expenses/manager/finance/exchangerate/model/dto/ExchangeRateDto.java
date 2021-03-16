package mm.expenses.manager.finance.exchangerate.model.dto;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.time.LocalDate;
import java.util.Objects;

@Data
public class ExchangeRateDto {

    private final CurrencyCode currency;

    private final LocalDate date;

    private final Double rate;

    @Builder(toBuilder = true)
    public ExchangeRateDto(final CurrencyCode currency, final LocalDate date, final Double rate) {
        this.currency = currency;
        this.date = date;
        this.rate = Objects.nonNull(rate) ? rate : 0.0;
    }

}
