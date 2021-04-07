package mm.expenses.manager.finance.exchangerate.provider;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.api.ApiInternalErrorException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
    private CurrencyRateProvider<? extends CurrencyRate> provider;

    CurrencyProviders(final Collection<CurrencyRateProvider<? extends CurrencyRate>> providers, final CurrencyRatesConfig config) {
        this.providers = providers.stream().collect(Collectors.toMap(DefaultCurrencyProvider::getName, Function.identity()));
        this.config = config;
    }

    @PostConstruct
    private void initializeDefaultProvider() {
        this.provider = findDefaultProviderOrAny();
    }

    /**
     * Get currently used currency provider name.
     */
    public CurrencyRateProvider<? extends CurrencyRate> getProvider() {
        return provider;
    }

    /**
     * Get currently used currency provider name.
     */
    public String getProviderName() {
        return getProviderConfig().getName();
    }

    /**
     * Get currently used currency type.
     */
    public CurrencyCode getCurrency() {
        return getProviderConfig().getCurrency();
    }

    /**
     * Get configuration for currently used currencies.
     */
    public ProviderConfig getProviderConfig() {
        return provider.getProviderConfig();
    }

    /**
     * Get global configuration for currencies.
     */
    public CurrencyRatesConfig getGlobalConfig() {
        return config;
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

    /**
     * Check if any provider is currently available or not.
     */
    private boolean isAnyProviderAvailable() {
        return !providers.isEmpty();
    }

    private ApiInternalErrorException apiInternalErrorExceptionForNoProvider() {
        return new ApiInternalErrorException("exchange-rate-provider", "Cannot find proper exchange rate provider.");
    }

    /**
     * Retrieve default provider if available or any.
     */
    private CurrencyRateProvider<? extends CurrencyRate> findDefaultProviderOrAny() {
        if (!isAnyProviderAvailable()) {
            throw apiInternalErrorExceptionForNoProvider();
        }
        return providers.getOrDefault(
                getGlobalConfig().getDefaultProvider(),
                providers.values().stream().findAny().orElseThrow(this::apiInternalErrorExceptionForNoProvider)
        );
    }

}
