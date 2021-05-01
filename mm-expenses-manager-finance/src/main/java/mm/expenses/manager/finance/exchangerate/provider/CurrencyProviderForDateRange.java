package mm.expenses.manager.finance.exchangerate.provider;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exception.CurrencyProviderException;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public interface CurrencyProviderForDateRange<T extends CurrencyRate> {

    Optional<T> getCurrencyRateForDate(final CurrencyCode currencyCode, final LocalDate date) throws CurrencyProviderException;

    Collection<T> getCurrencyRateForDateRange(final CurrencyCode currencyCode, final LocalDate from, final LocalDate to) throws CurrencyProviderException;

    Collection<T> getCurrencyRatesForDate(final LocalDate date) throws CurrencyProviderException;

    Collection<T> getCurrencyRatesForDateRange(final LocalDate from, final LocalDate to) throws CurrencyProviderException;

}
