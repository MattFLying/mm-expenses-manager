package mm.expenses.manager.finance.exchangerate.provider;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.beans.exception.api.ApiInternalErrorException;
import mm.expenses.manager.finance.currency.CurrencyRatesConfig;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
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

    public CurrencyProviders(final Collection<CurrencyRateProvider<? extends CurrencyRate>> providers, final CurrencyRatesConfig config) {
        this.providers = providers.stream().collect(Collectors.toMap(singleProvider -> singleProvider.getProviderConfig().getName(), Function.identity()));
        this.config = config;
    }

    @PostConstruct
    void initializeDefaultProvider() {
        this.provider = findDefaultProviderOrAny();
    }

    /**
     * Get all available providers.
     */
    public Map<String, CurrencyRateProvider<? extends CurrencyRate>> getProviders() {
        return providers;
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
     * Get the name of provider.
     *
     * @param provider some provider to check
     */
    public String getProviderName(final CurrencyRateProvider<? extends CurrencyRate> provider) {
        return provider.getProviderConfig().getName();
    }

    /**
     * Get currently used currency type.
     */
    public CurrencyCode getCurrency() {
        return getGlobalConfig().getDefaultCurrency();
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
        getProviders().values().forEach(operationToExecuteOnProvider);
    }

    /**
     * Process any action on all available providers with specific filters.
     */
    public void executeOnAllProviders(final Predicate<CurrencyRateProvider<? extends CurrencyRate>> filterProviders, final Consumer<CurrencyRateProvider<? extends CurrencyRate>> operationToExecuteOnProvider) {
        getProviders().values().stream().filter(filterProviders).forEach(operationToExecuteOnProvider);
    }

    /**
     * Check if any provider is currently available or not.
     */
    private boolean isAnyProviderAvailable() {
        return !getProviders().isEmpty();
    }

    private ApiInternalErrorException apiInternalErrorExceptionForNoProvider() {
        return new ApiInternalErrorException(FinanceExceptionMessage.CURRENCY_PROVIDER_NOT_FOUND);
    }

    /**
     * Retrieve default provider if available or any.
     */
    CurrencyRateProvider<? extends CurrencyRate> findDefaultProviderOrAny() {
        if (!isAnyProviderAvailable()) {
            throw apiInternalErrorExceptionForNoProvider();
        }
        return getProviders().getOrDefault(
                getGlobalConfig().getDefaultProvider(),
                getProviders().values().stream().findAny().orElseThrow(this::apiInternalErrorExceptionForNoProvider)
        );
    }

}
