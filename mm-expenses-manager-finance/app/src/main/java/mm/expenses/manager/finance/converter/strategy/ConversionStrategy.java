package mm.expenses.manager.finance.converter.strategy;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.finance.converter.CurrencyConversion.CurrencyRate;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Interface for different types of currency conversion
 */
public interface ConversionStrategy {

    /**
     * @return strategy type
     */
    ConversionStrategyType getStrategy();

    /**
     * Convert currency for latest date
     *
     * @param from  currency from which will be made conversion
     * @param to    currency to which will be made conversion
     * @param value value to be converted
     * @return converted currency
     */
    CurrencyRate convert(final CurrencyCode from, final CurrencyCode to, final BigDecimal value);

    /**
     * Convert currency for specific date
     *
     * @param from  currency from which will be made conversion
     * @param to    currency to which will be made conversion
     * @param value value to be converted
     * @param date  date for which the exchange rate should be taken
     * @return converted currency
     */
    CurrencyRate convert(final CurrencyCode from, final CurrencyCode to, final BigDecimal value, final LocalDate date);

}
