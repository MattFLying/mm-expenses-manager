package mm.expenses.manager.finance.cache.exchangerate.latest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheService;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class LatestRatesCacheService {

    private static final long DAYS_TO_FIND_IN_PAST = 10L;

    private final CurrencyRatesConfig currencyRatesConfig;
    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateCacheService exchangeRateCacheService;

    /**
     * @return Paged the latest available exchange rates.
     */
    public Collection<ExchangeRateCache> getLatest() {
        return exchangeRateCacheService.findAllLatest();
    }

    /**
     * @param currencyCodes set of currency codes
     * @return latest exchange rates as map for set of curreny codes
     */
    public Map<CurrencyCode, ExchangeRateCache> getLatest(final Set<CurrencyCode> currencyCodes) {
        return exchangeRateCacheService.findAllLatestOfCurrencies(currencyCodes).stream().collect(Collectors.toMap(ExchangeRateCache::getCurrency, Function.identity()));
    }

    /**
     * @param currency currency code
     * @return The latest available exchange rate for given currency.
     */
    public Optional<ExchangeRateCache> getLatest(final CurrencyCode currency) {
        return exchangeRateCacheService.findLatestForCurrency(currency);
    }

    /**
     * Save the latest currency rates into memory cache. Method is called during {@link ContextRefreshedEvent} execution
     * and when synchronization process successfully saved current rates {@link UpdateLatestInMemoryEvent}
     */
    @EventListener({ContextRefreshedEvent.class, UpdateLatestInMemoryEvent.class})
    public void saveInMemory() {
        log.info("Latest exchange rates saving into memory of {}.", exchangeRateCacheService.getCacheType());
        final var freshLatest = findLatestAvailableByDate().lastEntry().getValue().stream().collect(Collectors.toMap(ExchangeRate::getId, Function.identity()));
        final var latestInCache = getLatest().stream().collect(Collectors.toMap(ExchangeRateCache::getId, Function.identity()));

        if (isCacheUpToDate(freshLatest, latestInCache)) {
            log.info("Update cache won't be executed, latest exchange rates in memory are up to date.");
            return;
        }

        if (!latestInCache.isEmpty()) {
            exchangeRateCacheService.disableLatest(latestInCache.values());
        }
        exchangeRateCacheService.saveFresh(freshLatest.values());
    }

    protected Set<CurrencyCode> getAllRequiredCurrenciesCode() {
        return currencyRatesConfig.getAllRequiredCurrenciesCode();
    }

    protected NavigableMap<Instant, List<ExchangeRate>> findLatestAvailableByDate() {
        final var requiredCurrencies = getAllRequiredCurrenciesCode();
        final var pageRequest = exchangeRateService.pageRequest(0, requiredCurrencies.size());
        NavigableMap<Instant, List<ExchangeRate>> allLatest = findAllLatest(pageRequest);

        final var now = DateUtils.instantToLocalDateUTC(DateUtils.now());
        var from = now.minusDays(DAYS_TO_FIND_IN_PAST);
        var to = LocalDate.from(now);
        var notFoundStepCount = 0L;

        while (allLatest.isEmpty()) {
            final var minusDays = notFoundStepCount * DAYS_TO_FIND_IN_PAST;
            allLatest = findAll(pageRequest, from, to, minusDays);
            notFoundStepCount++;
        }
        return allLatest;
    }

    private boolean isCacheUpToDate(final Map<String, ExchangeRate> fresh, final Map<String, ExchangeRateCache> cache) {
        final var freshLatestIds = fresh.keySet();
        final var inCacheIds = cache.keySet();

        return freshLatestIds.size() == inCacheIds.size() && CollectionUtils.containsAll(freshLatestIds, inCacheIds);
    }

    private TreeMap<Instant, List<ExchangeRate>> findAll(final PageRequest pageRequest, final LocalDate from, final LocalDate to, final long minusDays) {
        return exchangeRateService.findAll(null, from.minusDays(minusDays), to.minusDays(minusDays), pageRequest)
                .map(Slice::getContent)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(ExchangeRate::getDate, TreeMap::new, Collectors.toList()));
    }

    private TreeMap<Instant, List<ExchangeRate>> findAllLatest(final PageRequest pageRequest) {
        return exchangeRateService.findByDate(pageRequest, DateUtils.instantToLocalDateUTC(Instant.now()))
                .stream()
                .collect(Collectors.groupingBy(ExchangeRate::getDate, TreeMap::new, Collectors.toList()));
    }

}
