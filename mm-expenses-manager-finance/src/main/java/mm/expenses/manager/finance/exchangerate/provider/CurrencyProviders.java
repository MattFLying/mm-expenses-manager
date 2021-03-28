package mm.expenses.manager.finance.exchangerate.provider;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.ApiInternalErrorException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Contains all available currency providers with its settings.
 */
@Component
public class CurrencyProviders {

    private final Map<String, CurrencyRateProvider<? extends CurrencyRate>> providers;
    private final CurrencyRatesConfig config;

    CurrencyProviders(final Collection<CurrencyRateProvider<? extends CurrencyRate>> providers, final CurrencyRatesConfig config) {
        this.providers = providers.stream().collect(Collectors.toMap(DefaultCurrencyProvider::getName, Function.identity()));
        this.config = config;
    }

    /**
     * Get default currency provider name.
     */
    public String getDefaultProvider() {
        return config.getDefaultProvider();
    }

    /**
     * Get default currency type.
     */
    public CurrencyCode getDefaultCurrency() {
        return CurrencyCode.valueOf(config.getDefaultCurrency());
    }

    /**
     * Get configuration for currencies.
     */
    public CurrencyRatesConfig getConfig() {
        return config;
    }

    /**
     * Check if any provider is currently available or not.
     */
    public boolean isAnyProviderAvailable() {
        return !providers.isEmpty();
    }

    /**
     * Retrieve default provider if available or any.
     */
    public CurrencyRateProvider<? extends CurrencyRate> findDefaultProviderOrAny() {
        return providers.getOrDefault(
                config.getDefaultProvider(),
                providers.values().stream().findAny().orElseThrow(this::apiInternalErrorExceptionForNoProvider)
        );
    }

    /**
     * Process any action on all available providers.
     */
    public void executeOnAllProviders(final Consumer<CurrencyRateProvider<? extends CurrencyRate>> operationToExecuteOnProvider) {
        providers.values().forEach(operationToExecuteOnProvider);
    }

    /**
     * Process any action on all available providers with specific filters.
     */
    public void executeOnAllProviders(final Predicate<CurrencyRateProvider<? extends CurrencyRate>> filterProviders, final Consumer<CurrencyRateProvider<? extends CurrencyRate>> operationToExecuteOnProvider) {
        providers.values().stream().filter(filterProviders).forEach(operationToExecuteOnProvider);
    }

    public ApiInternalErrorException apiInternalErrorExceptionForNoProvider() {
        return new ApiInternalErrorException("exchange-rate-provider", "Cannot find proper exchange rate provider.");
    }

}
