package mm.expenses.manager.finance.exchangerate.latest;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Default cache mechanism based on simple map. If there is no another cache mechanism defined and marked as working
 * then this one will be used.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.configuration", name = "cache", havingValue = "map", matchIfMissing = true)
class LatestCacheMap extends LatestCacheInit {

    private final Map<CurrencyCode, ExchangeRate> latest;

    LatestCacheMap(final CurrencyRatesConfig config, final ExchangeRateService service) {
        super(config, service);
        this.latest = new EnumMap<>(CurrencyCode.class);
    }

    @Override
    public Page<ExchangeRate> getLatest() {
        final var pageRequest = service.pageRequest(0, getAllRequiredCurrenciesCode().size());
        return new PageImpl<>(new ArrayList<>(latest.values()), pageRequest, latest.size());
    }

    @Override
    public Map<CurrencyCode, ExchangeRate> getLatest(final Set<CurrencyCode> currencyCodes) {
        return latest.entrySet()
                .stream()
                .filter(latestRate -> currencyCodes.contains(latestRate.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Optional<ExchangeRate> getLatest(final CurrencyCode currency) {
        return Optional.ofNullable(latest.get(currency));
    }

    @Override
    public void saveInMemory() {
        log.info("Latest exchange rates saving into memory map.");
        findLatestAvailableByDate().lastEntry().getValue().forEach(exchangeRate -> latest.compute(exchangeRate.getCurrency(), (key, value) -> exchangeRate));
    }

}
