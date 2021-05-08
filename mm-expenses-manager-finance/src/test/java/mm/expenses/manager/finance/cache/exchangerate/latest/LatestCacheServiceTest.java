package mm.expenses.manager.finance.cache.exchangerate.latest;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheService;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.PROVIDER_NAME;

@Slf4j
@Component
@Profile("test")
@ConditionalOnProperty(prefix = "app.configuration.cache", name = "type", havingValue = "map", matchIfMissing = true)
public class LatestCacheServiceTest extends LatestRatesCacheService {

    final Map<CurrencyCode, ExchangeRateCache> map = new HashMap<>();

    LatestCacheServiceTest(final CurrencyRatesConfig config, final ExchangeRateService service, final ExchangeRateCacheService exchangeRateCacheService) {
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
