package mm.expenses.manager.finance.exchangerate.provider;

import lombok.Data;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
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

    private final BigDecimal rate;

    private final Map<String, Object> details;

    protected CurrencyRate(final CurrencyCode currency, final LocalDate date, final BigDecimal rate, final Map<String, Object> details) {
        this.currency = CurrencyCode.of(currency);
        this.date = date;
        this.rate = BigDecimalWrapper.of(rate);
        this.details = Objects.nonNull(details) ? details : new HashMap<>();
    }

    public Optional<Object> getDetails(final String key) {
        return Optional.ofNullable(details.get(key));
    }

    public static <T extends CurrencyRate> Comparator<T> currencyRateComparator() {
        return Comparator.comparing(T::getCurrency).thenComparing(T::getDate);
    }

}
