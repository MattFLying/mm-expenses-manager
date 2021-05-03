package mm.expenses.manager.finance.cache.exchangerate.latest;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheService;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cache mechanism with Redis.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.configuration.cache", name = "type", havingValue = "redis")
class LatestCacheRedis extends LatestCacheInit {

    LatestCacheRedis(final CurrencyRatesConfig config, final ExchangeRateService service, final ExchangeRateCacheService exchangeRateCacheService) {
        super(config, service, exchangeRateCacheService);
    }

    @Override
    public Collection<ExchangeRateCache> getLatest() {
        return exchangeRateCacheService.findAllLatest();
    }

    @Override
    public Map<CurrencyCode, ExchangeRateCache> getLatest(final Set<CurrencyCode> currencyCodes) {
        return exchangeRateCacheService.findAllLatestOfCurrencies(currencyCodes).stream().collect(Collectors.toMap(ExchangeRateCache::getCurrency, Function.identity()));
    }

    @Override
    public Optional<ExchangeRateCache> getLatest(final CurrencyCode currency) {
        return exchangeRateCacheService.findLatestForCurrency(currency);
    }

    @Override
    protected CacheType cacheType() {
        return CacheType.REDIS;
    }

}
