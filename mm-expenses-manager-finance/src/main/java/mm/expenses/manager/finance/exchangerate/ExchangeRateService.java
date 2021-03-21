package mm.expenses.manager.finance.exchangerate;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.ApiInternalErrorException;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
import mm.expenses.manager.finance.exchangerate.provider.DefaultCurrencyProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
class ExchangeRateService {

    private final Map<String, CurrencyRateProvider<? extends CurrencyRate>> providers;
    private final ExchangeRateCommand command;
    private final ExchangeRateQuery query;
    private final ExchangeRateConfig config;

    public ExchangeRateService(final Collection<CurrencyRateProvider<? extends CurrencyRate>> providers, final ExchangeRateCommand command, final ExchangeRateQuery query, final ExchangeRateConfig config) {
        this.command = command;
        this.query = query;
        this.config = config;
        this.providers = providers.stream().collect(Collectors.toMap(DefaultCurrencyProvider::getName, Function.identity()));
    }

    void historyUpdate() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (providers.isEmpty()) {
                throw apiInternalErrorExceptionForNoProvider();
            }
            try {
                final var provider = findCurrentProviderOrAny();
                log.info("Currencies history update in progress.");
                command.saveHistory(provider.getAllHistoricalCurrencies());
                log.info("Currencies history update has been done.");
            } catch (final Exception unknownException) {
                log.warn("Error occurred during currencies history update process.", unknownException);
                providers.values().forEach(provider -> {
                    log.info("Currencies history update retrying for another provider: {} in progress.", provider.getName());
                    command.saveHistory(provider.getAllHistoricalCurrencies());
                    log.info("Currencies history update retried for another provider: {}  has been done.", provider.getName());
                });
            }
        });
    }

    void saveAllCurrent() {
        if (providers.isEmpty()) {
            throw apiInternalErrorExceptionForNoProvider();
        }

        final var provider = findCurrentProviderOrAny();
        final var allCurrent = provider.getCurrentCurrencyRates();
        if (!allCurrent.isEmpty()) {
            command.createOrUpdate(allCurrent);
        } else {
            providers.values()
                    .stream()
                    .filter(otherProvider -> !otherProvider.getName().equalsIgnoreCase(config.getDefaultProvider()))
                    .forEach(otherProvider -> command.createOrUpdate(otherProvider.getCurrentCurrencyRates()));
        }
    }

    Stream<ExchangeRate> findAll(final LocalDate date, final LocalDate from, final LocalDate to) {
        return query.findAllCurrenciesRates(date, from, to);
    }

    Stream<ExchangeRate> findAllForCurrency(final CurrencyCode currency, final LocalDate from, final LocalDate to) {
        return query.findAllForCurrencyRates(currency, from, to);
    }

    Stream<ExchangeRate> findLatest() {
        return query.findAllLatest();
    }

    Optional<ExchangeRate> findLatestForCurrency(final CurrencyCode currency) {
        return query.findLatestForCurrency(currency);
    }

    Optional<ExchangeRate> findForCurrencyAndSpecificDate(final CurrencyCode currency, final LocalDate date) {
        return query.findByCurrencyAndDate(currency, date);
    }

    private CurrencyRateProvider<? extends CurrencyRate> findCurrentProviderOrAny() {
        return providers.getOrDefault(
                config.getDefaultProvider(),
                providers.values().stream().findAny().orElseThrow(this::apiInternalErrorExceptionForNoProvider)
        );
    }

    private ApiInternalErrorException apiInternalErrorExceptionForNoProvider() {
        return new ApiInternalErrorException("exchange-rate-provider", "Cannot find proper exchange rate provider.");
    }

}
