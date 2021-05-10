package mm.expenses.manager.finance.exchangerate.provider;

public interface DefaultCurrencyProvider<T extends CurrencyRate> {

    ProviderConfig getProviderConfig();

    default String getName() {
        return getProviderConfig().getName();
    }

}
