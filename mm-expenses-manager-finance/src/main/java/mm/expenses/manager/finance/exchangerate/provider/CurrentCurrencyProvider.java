package mm.expenses.manager.finance.exchangerate.provider;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exception.CurrencyProviderException;

import java.util.Collection;
import java.util.Optional;

public interface CurrentCurrencyProvider<T extends CurrencyRate> {

    Optional<T> getCurrentCurrencyRate(final CurrencyCode currencyCode) throws CurrencyProviderException;

    Collection<T> getCurrentCurrencyRates() throws CurrencyProviderException;

}
