package mm.expenses.manager.finance.cache.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Copy of {@link ExchangeRateCacheMapRepository} for testing
 */
@Component
@ConditionalOnProperty(prefix = "app.configuration.cache", name = "type", havingValue = "test", matchIfMissing = true)
class ExchangeRateCacheTestRepository implements ExchangeRateCacheRepository {

    private final Map<String, ExchangeRateCache> cache = new HashMap<>();

    @Override
    public Optional<ExchangeRateCache> findByCurrencyAndIsLatestTrue(final CurrencyCode currency) {
        return cache.values()
                .stream()
                .filter(rateCache -> rateCache.getCurrency().equals(currency))
                .filter(ExchangeRateCache::isLatest)
                .findFirst();
    }

    @Override
    public Optional<ExchangeRateCache> findByCurrencyAndDate(final CurrencyCode currency, final LocalDate date) {
        return cache.values()
                .stream()
                .filter(rateCache -> rateCache.getCurrency().equals(currency) && rateCache.getDate().equals(date))
                .findFirst();
    }

    @Override
    public Collection<ExchangeRateCache> findByCurrencyInAndDate(final Set<CurrencyCode> currencyCodes, final LocalDate date) {
        return cache.values()
                .stream()
                .filter(rateCache -> currencyCodes.contains(rateCache.getCurrency()))
                .filter(rateCache -> rateCache.getDate().equals(date))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ExchangeRateCache> findByCurrencyInAndIsLatestTrue(final Set<CurrencyCode> currencyCodes) {
        return cache.values()
                .stream()
                .filter(rateCache -> currencyCodes.contains(rateCache.getCurrency()))
                .filter(ExchangeRateCache::isLatest)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ExchangeRateCache> findByIsLatestTrue() {
        return cache.values().stream()
                .filter(ExchangeRateCache::isLatest)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ExchangeRateCache> findByIsLatestFalse() {
        return cache.values().stream()
                .filter(rateCache -> !rateCache.isLatest())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ExchangeRateCache> findAll() {
        return cache.values();
    }

    @Override
    public <S extends ExchangeRateCache> Iterable<S> saveAll(final Iterable<S> toSave) {
        final var toBeSaved = IterableUtils.toList(toSave);
        cache.putAll(toBeSaved.stream().collect(Collectors.toMap(ExchangeRateCache::getId, Function.identity())));
        return toBeSaved;
    }

    @Override
    public void deleteAll(final Iterable<? extends ExchangeRateCache> toRemove) {
        cache.values().removeAll(IterableUtils.toList(toRemove));
    }

    @Override
    public void deleteAll() {
        cache.values().clear();
    }

}
