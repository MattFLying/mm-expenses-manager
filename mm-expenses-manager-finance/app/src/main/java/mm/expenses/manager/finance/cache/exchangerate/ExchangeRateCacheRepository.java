package mm.expenses.manager.finance.cache.exchangerate;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for all needed operations on cache should be implemented by all available cache types.
 */
interface ExchangeRateCacheRepository {

    Optional<ExchangeRateCache> findByCurrencyAndIsLatestTrue(final CurrencyCode currency);

    Optional<ExchangeRateCache> findByCurrencyAndDate(final CurrencyCode currency, final LocalDate date);

    Collection<ExchangeRateCache> findByCurrencyInAndDate(final Set<CurrencyCode> currencyCodes, final LocalDate date);

    Collection<ExchangeRateCache> findByCurrencyInAndIsLatestTrue(final Set<CurrencyCode> currencyCodes);

    Collection<ExchangeRateCache> findByIsLatestTrue();

    Collection<ExchangeRateCache> findByIsLatestFalse();

    <S extends ExchangeRateCache> Iterable<S> saveAll(final Iterable<S> toSave);

    void deleteAll(final Iterable<? extends ExchangeRateCache> toRemove);

    void deleteAll();

    CacheType getCacheType();

    enum CacheType {
        REDIS, MAP
    }

}
