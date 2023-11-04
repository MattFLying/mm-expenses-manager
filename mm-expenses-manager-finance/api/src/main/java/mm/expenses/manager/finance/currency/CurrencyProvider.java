package mm.expenses.manager.finance.currency;

import mm.expenses.manager.common.i18n.CurrencyCode;

public interface CurrencyProvider {

    /**
     * Returns currently used currency as default in application.
     *
     * @return default currency
     */
    CurrencyCode getCurrentCurrency();

}
