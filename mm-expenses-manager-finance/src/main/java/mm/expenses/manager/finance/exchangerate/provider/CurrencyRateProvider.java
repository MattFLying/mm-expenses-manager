package mm.expenses.manager.finance.exchangerate.provider;

import java.util.Collection;

/**
 * Currency rates provider strategy. Should be implemented for all possible ways to fetch the currency rates
 * from different sources
 *
 * @param <T> custom currency rate implementation represents single currency rate
 */
public interface CurrencyRateProvider<T extends CurrencyRate> extends DefaultCurrencyProvider<T>, CurrentCurrencyProvider<T>, CurrencyProviderForDateRange<T> {

    HistoricCurrencies<T> getHistoricCurrencies();

    default Collection<T> getAllHistoricalCurrencies() {
        return getHistoricCurrencies().fetchHistoricalCurrencies();
    }

}
