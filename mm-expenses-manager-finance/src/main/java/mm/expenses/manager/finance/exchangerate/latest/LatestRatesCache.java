package mm.expenses.manager.finance.exchangerate.latest;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Cache for current exchange rates.
 */
public interface LatestRatesCache {

    /**
     * @return Paged the latest available exchange rates.
     */
    Page<ExchangeRate> getLatest();

    /**
     * @param currency currency code
     * @return The latest available exchange rate for given currency.
     */
    Optional<ExchangeRate> getLatest(final CurrencyCode currency);

    /**
     * @param currencyCodes set of currency codes
     * @return latest exchange rates as map for set of curreny codes
     */
    Map<CurrencyCode, ExchangeRate> getLatest(final Set<CurrencyCode> currencyCodes);

    /**
     * Save the latest currency rates into memory cache. Method is called during {@link ContextRefreshedEvent} execution
     * and when synchronization process successfully saved current rates {@link UpdateLatestInMemoryEvent}
     */
    @EventListener({ContextRefreshedEvent.class, UpdateLatestInMemoryEvent.class})
    void saveInMemory();

}
