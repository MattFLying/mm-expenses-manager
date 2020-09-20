package mm.expenses.manager.finance.financial;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.common.CurrencyProviderType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

/**
 * Currency rates provider strategy. Should be implemented for all possible ways to fetch the currency rates
 * from different sources
 *
 * @param <T> custom currency rate implementation represents single currency rate
 */
public interface CurrencyRateProvider<T extends CurrencyRate> {

    CurrencyCode getDefaultCurrency();

    Optional<T> getCurrentCurrencyRate(final CurrencyCode currencyCode);

    Optional<T> getCurrencyRateForDate(final CurrencyCode currencyCode, final LocalDate date);

    Collection<T> getCurrencyRateForDateRange(final CurrencyCode currencyCode, final LocalDate from, final LocalDate to);

    Collection<T> getCurrentCurrencyRates();

    Collection<T> getCurrencyRatesForDate(final LocalDate date);

    Collection<T> getCurrencyRatesForDateRange(final LocalDate from, final LocalDate to);

    /**
     * Specific details of currency provided by currency provider
     */
    interface CurrencyDetails {

        CurrencyProviderType getType();

    }

}
