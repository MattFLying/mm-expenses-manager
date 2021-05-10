package mm.expenses.manager.finance.cache.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheRepository.CacheType;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateCacheService {

    private final ExchangeRateCacheRepository repository;
    private final CurrencyProviders currencyProviders;

    public Optional<ExchangeRateCache> findLatestForCurrency(final CurrencyCode currency) {
        return repository.findByCurrencyAndIsLatestTrue(currency);
    }

    public Collection<ExchangeRateCache> findAllLatestOfCurrencies(final Set<CurrencyCode> currencyCodes) {
        return repository.findByCurrencyInAndIsLatestTrue(currencyCodes);
    }

    public Collection<ExchangeRateCache> findAllLatest() {
        return repository.findByIsLatestTrue();
    }

    public Optional<ExchangeRateCache> findForCurrencyAndSpecificDate(final CurrencyCode currency, final LocalDate date) {
        return repository.findByCurrencyAndDate(currency, date);
    }

    public Collection<ExchangeRateCache> findForCurrencyCodesAndSpecificDate(final Set<CurrencyCode> currencyCodes, final LocalDate date) {
        return repository.findByCurrencyInAndDate(currencyCodes, date);
    }

    public void disableLatest(final Collection<ExchangeRateCache> cacheRates) {
        repository.saveAll(cacheRates.stream().map(ExchangeRateCache::disableLatest).collect(Collectors.toList()));
    }

    public void saveFresh(final Collection<ExchangeRate> freshRates) {
        saveFresh(freshRates, true);
    }

    public void saveFresh(final Collection<ExchangeRate> freshRates, final boolean isLatest) {
        repository.saveAll(freshRates.stream().map(exchangeRate -> ExchangeRateCache.of(exchangeRate, isLatest, currencyProviders.getProviderName())).collect(Collectors.toList()));
    }

    public CacheType getCacheType() {
        return repository.getCacheType();
    }

    void clearCache() {
        log.info("Clearing cache in progress.");
        repository.deleteAll();
        log.info("Clearing cache completed.");
    }

    @Scheduled(cron = "${app.configuration.cache.clear-cron}")
    void clearNotLatestCache() {
        log.info("Clearing cache for not latest rates in progress.");
        repository.deleteAll(repository.findByIsLatestFalse());
        log.info("Clearing cache for not latest rates completed.");
    }

}
