package mm.expenses.manager.finance.exchangerate.provider;

import mm.expenses.manager.common.i18n.CurrencyCode;

public interface DefaultCurrencyProvider<T extends CurrencyRate> {

    ProviderConfig getProviderConfig();

    default String getName() {
        return getProviderConfig().getName();
    }

    default CurrencyCode getDefaultCurrency() {
        return CurrencyCode.getCurrencyFromString(getProviderConfig().getDefaultCurrency());
    }

}
