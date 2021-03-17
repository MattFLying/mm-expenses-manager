package mm.expenses.manager.finance.exchangerate.provider;

import mm.expenses.manager.common.i18n.CurrencyCode;

import java.util.Collection;
import java.util.Optional;

public interface CurrentCurrencyProvider<T extends CurrencyRate> {

    Optional<T> getCurrentCurrencyRate(final CurrencyCode currencyCode);

    Collection<T> getCurrentCurrencyRates();

}
