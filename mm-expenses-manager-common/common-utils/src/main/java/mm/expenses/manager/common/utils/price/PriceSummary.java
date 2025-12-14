package mm.expenses.manager.common.utils.price;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;

/**
 * Simple interface to be implemented to handle prices summary that may be different for different cases.
 */
public interface PriceSummary {

    /**
     * Returns prices summary as array because it may contain prices for different currencies.
     */
    Prices getPriceSummary();

    /**
     * Returns price summary for specific currency.
     */
    Price getPriceSummary(final CurrencyCode currency);

}
