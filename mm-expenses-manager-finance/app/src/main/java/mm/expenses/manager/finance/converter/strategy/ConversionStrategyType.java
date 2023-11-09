package mm.expenses.manager.finance.converter.strategy;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;

/**
 * Conversion strategy
 */
public enum ConversionStrategyType {
    /**
     * Conversion from default app currency to another
     */
    FROM_DEFAULT,

    /**
     * Conversion from different currency to default app currency
     */
    TO_DEFAULT,

    /**
     * Conversion from different currency to different one that. Both are not the default app currency
     */
    DIFFERENT;

    /**
     * Find strategy type
     *
     * @param defaultCurrency default currency in app
     * @param from            currency from which conversion should be made
     * @param to              currency to which conversion should be made
     * @return strategy type
     */
    public static ConversionStrategyType findConversionStrategy(final CurrencyCode defaultCurrency, final CurrencyCode from, final CurrencyCode to) {
        if (from.equals(defaultCurrency) && !to.equals(defaultCurrency)) {
            return ConversionStrategyType.FROM_DEFAULT;
        }
        if (!from.equals(defaultCurrency) && to.equals(defaultCurrency)) {
            return ConversionStrategyType.TO_DEFAULT;
        }
        return ConversionStrategyType.DIFFERENT;
    }

}
