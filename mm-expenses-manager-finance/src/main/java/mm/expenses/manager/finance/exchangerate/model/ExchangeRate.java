package mm.expenses.manager.finance.exchangerate.model;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class ExchangeRate {

    private final String id;

    private final CurrencyCode currency;

    private final LocalDate date;

    private final Double rate;

    private final Instant createdAt;

}
