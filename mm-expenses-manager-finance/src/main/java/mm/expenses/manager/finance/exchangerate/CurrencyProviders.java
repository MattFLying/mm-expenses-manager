package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.exception.ApiInternalErrorException;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
import mm.expenses.manager.finance.exchangerate.provider.DefaultCurrencyProvider;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
class CurrencyProviders {

    private final Map<String, CurrencyRateProvider<? extends CurrencyRate>> providers;
    private final ExchangeRateConfig config;

    CurrencyProviders(final Collection<CurrencyRateProvider<? extends CurrencyRate>> providers, final ExchangeRateConfig config) {
        this.providers = providers.stream().collect(Collectors.toMap(DefaultCurrencyProvider::getName, Function.identity()));
        this.config = config;
    }

    String getDefaultProvider() {
        return config.getDefaultProvider();
    }

    ExchangeRateConfig getConfig() {
        return config;
    }

    boolean isAnyProviderAvailable() {
        return !providers.isEmpty();
    }

    CurrencyRateProvider<? extends CurrencyRate> findCurrentProviderOrAny() {
        return providers.getOrDefault(
                config.getDefaultProvider(),
                providers.values().stream().findAny().orElseThrow(this::apiInternalErrorExceptionForNoProvider)
        );
    }

    void executeOnAllProviders(final Consumer<CurrencyRateProvider<? extends CurrencyRate>> operationToExecuteOnProvider) {
        providers.values().forEach(operationToExecuteOnProvider);
    }

    void executeOnAllProviders(final Predicate<CurrencyRateProvider<? extends CurrencyRate>> filterProviders, final Consumer<CurrencyRateProvider<? extends CurrencyRate>> operationToExecuteOnProvider) {
        providers.values().stream().filter(filterProviders).forEach(operationToExecuteOnProvider);
    }

    ApiInternalErrorException apiInternalErrorExceptionForNoProvider() {
        return new ApiInternalErrorException("exchange-rate-provider", "Cannot find proper exchange rate provider.");
    }

}
