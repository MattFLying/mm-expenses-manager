package mm.expenses.manager.finance.exchangerate.provider;

import mm.expenses.manager.common.i18n.CurrencyCode;

public interface DefaultCurrencyProvider<T extends CurrencyRate> {

    String getName();

    CurrencyCode getDefaultCurrency();

}
