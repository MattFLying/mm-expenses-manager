package mm.expenses.manager.finance.exchangerate.model;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Data
public class ExchangeRates {

    private final CurrencyCode currency;

    private final Collection<ExchangeRate> rates;

    @Builder(toBuilder = true)
    public ExchangeRates(final CurrencyCode currency, final Collection<ExchangeRate> rates) {
        this.currency = currency;
        this.rates = Objects.nonNull(rates) ? rates : new ArrayList<>();
    }

}
