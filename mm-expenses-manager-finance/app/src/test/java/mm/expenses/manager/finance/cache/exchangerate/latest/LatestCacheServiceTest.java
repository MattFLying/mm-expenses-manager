package mm.expenses.manager.finance.cache.exchangerate.latest;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheService;
import mm.expenses.manager.finance.currency.CurrenciesService;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.PROVIDER_NAME;

@Slf4j
@Component
@Profile("test")
public class LatestCacheServiceTest extends LatestRatesCacheService {

    final Map<CurrencyCode, ExchangeRateCache> map = new HashMap<>();

    LatestCacheServiceTest(final CurrenciesService currenciesService, final ExchangeRateService service, final ExchangeRateCacheService exchangeRateCacheService) {
        super(currenciesService, service, exchangeRateCacheService);
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
