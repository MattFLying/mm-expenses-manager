package mm.expenses.manager.finance.exchangerate.provider;

import mm.expenses.manager.finance.exception.CurrencyProviderException;

import java.time.LocalDate;
import java.util.Collection;

/**
 * Currency rates provider strategy. Should be implemented for all possible ways to fetch the currency rates
 * from different sources
 *
 * @param <T> custom currency rate implementation represents single currency rate
 */
public interface CurrencyRateProvider<T extends CurrencyRate> {

    ProviderConfig getProviderConfig();

    Collection<T> getCurrentCurrencyRates() throws CurrencyProviderException;

    Collection<T> getCurrencyRatesForDateRange(final LocalDate from, final LocalDate to) throws CurrencyProviderException;

    HistoricCurrencies<T> getHistoricCurrencies();

}
