package mm.expenses.manager.finance.exchangerate.provider;

import java.util.Collection;

/**
 * Interface for historical currencies handling with specific conditions for each provider
 *
 * @param <T> custom currency rate implementation represents single currency rate
 */
public interface HistoricCurrencies<T extends CurrencyRate> {

    Collection<T> fetchHistoricalCurrencies();

}
