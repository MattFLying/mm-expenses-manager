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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.configuration", name = "cache", havingValue = "test", matchIfMissing = true)
public class LatestCacheTest extends LatestCacheInit {

    final Map<CurrencyCode, ExchangeRate> map = new HashMap<>();

    LatestCacheTest(final CurrencyRatesConfig config, final ExchangeRateService service) {
        super(config, service);
    }

    @Override
    public Page<ExchangeRate> getLatest() {
        return new PageImpl<>(new ArrayList<>(map.values()));
    }

    @Override
    public Optional<ExchangeRate> getLatest(final CurrencyCode currency) {
        return Optional.ofNullable(map.get(currency));
    }

    @Override
    public void saveInMemory() {

    }

    public void saveInMemory(final CurrencyCode currencyCode, final ExchangeRate rate) {
        map.put(currencyCode, rate);
    }

    public void reset() {
        map.clear();
    }

}
