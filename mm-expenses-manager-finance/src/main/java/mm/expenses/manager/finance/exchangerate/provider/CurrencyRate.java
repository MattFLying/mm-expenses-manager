package mm.expenses.manager.finance.exchangerate.provider;

import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Abstract representation of the currency rate with default properties described the currency rate
 */
@Data
public abstract class CurrencyRate {

    private final CurrencyCode currency;

    private final LocalDate date;

    private final Double rate;

    private final Map<String, Object> details;

    public CurrencyRate(final CurrencyCode currency, final LocalDate date, final Double rate, final Map<String, Object> details) {
        this.currency = CurrencyCode.of(currency);
        this.date = date;
        this.rate = Objects.nonNull(rate) ? rate : 0.0;
        this.details = Objects.nonNull(details) ? details : new HashMap<>();
    }

    public Optional<Object> getDetails(final String key) {
        return Optional.ofNullable(details.get(key));
    }

    public void addDetails(final String key, final Object value) {
        details.put(key, value);
    }

}
