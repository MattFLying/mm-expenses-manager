package mm.expenses.manager.finance.exchangerate.latest;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cache mechanism with Redis.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.configuration", name = "cache", havingValue = "redis")
class LatestCacheRedis extends LatestCacheInit {

    private final RedisTemplate<CurrencyCode, ExchangeRate> redisTemplate;

    LatestCacheRedis(final CurrencyRatesConfig config, final ExchangeRateService service, final RedisTemplate<CurrencyCode, ExchangeRate> redisTemplate) {
        super(config, service);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Page<ExchangeRate> getLatest() {
        final var currencyCodes = getAllRequiredCurrenciesCode();
        final var pageRequest = service.pageRequest(0, currencyCodes.size());
        final var latest = CollectionUtils.emptyIfNull(redisTemplate.opsForValue().multiGet(currencyCodes)).stream().filter(Objects::nonNull).collect(Collectors.toList());
        return new PageImpl<>(latest, pageRequest, latest.size());
    }

    @Override
    public Optional<ExchangeRate> getLatest(final CurrencyCode currency) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(currency));
    }

    @Override
    public void saveInMemory() {
        log.info("Latest exchange rates saving into memory redis.");
        findLatestAvailableByDate().lastEntry().getValue().forEach(exchangeRate -> redisTemplate.opsForValue().set(exchangeRate.getCurrency(), exchangeRate));
    }

}
