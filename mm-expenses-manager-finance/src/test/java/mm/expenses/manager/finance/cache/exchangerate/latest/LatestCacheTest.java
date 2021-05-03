package mm.expenses.manager.finance.cache.exchangerate.latest;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheService;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.PROVIDER_NAME;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.configuration.cache", name = "type", havingValue = "test", matchIfMissing = true)
public class LatestCacheTest extends LatestCacheInit {

    final Map<CurrencyCode, ExchangeRateCache> map = new HashMap<>();

    LatestCacheTest(final CurrencyRatesConfig config, final ExchangeRateService service, final ExchangeRateCacheService exchangeRateCacheService) {
        super(config, service, exchangeRateCacheService);
    }

    @Override
    public Collection<ExchangeRateCache> getLatest() {
        return map.values();
    }

    @Override
    public Map<CurrencyCode, ExchangeRateCache> getLatest(final Set<CurrencyCode> currencyCodes) {
        return map.entrySet()
                .stream()
                .filter(latestRate -> currencyCodes.contains(latestRate.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Optional<ExchangeRateCache> getLatest(final CurrencyCode currency) {
        return Optional.ofNullable(map.get(currency));
    }

    @Override
    protected CacheType cacheType() {
        return CacheType.MAP;
    }

    @Override
    public void saveInMemory() {

    }

    public void saveInMemory(final CurrencyCode currencyCode, final ExchangeRate rate) {
        map.put(currencyCode, ExchangeRateCache.of(rate, PROVIDER_NAME));
    }

    public void saveInMemory(final CurrencyCode currencyCode, final ExchangeRateCache rate) {
        map.put(currencyCode, rate);
    }

    public void reset() {
        map.clear();
    }

}
