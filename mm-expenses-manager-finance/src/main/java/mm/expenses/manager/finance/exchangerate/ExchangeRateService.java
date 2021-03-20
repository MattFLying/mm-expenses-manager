package mm.expenses.manager.finance.exchangerate;

import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRates;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
class ExchangeRateService {

    private final CurrencyRateProvider<? extends CurrencyRate> provider;
    private final ExchangeRateCommand creator;
    private final ExchangeRateQuery finder;

    void historyUpdate() {
        Executors.newSingleThreadExecutor().execute(() -> {
            log.info("Currencies history update in progress.");
            creator.saveHistory(provider.getAllHistoricalCurrencies());
            log.info("Currencies history update has been done.");
        });
    }

    Collection<ExchangeRate> saveAllCurrent() {
        final var allCurrent = provider.getCurrentCurrencyRates();
        if (!allCurrent.isEmpty()) {
            return creator.createOrUpdate(allCurrent);
        }
        return Collections.emptyList();
    }

    Collection<ExchangeRates> findAll(final LocalDate date, final LocalDate from, final LocalDate to) {
        return finder.findAllCurrenciesRates(date, from, to);
    }

    Collection<ExchangeRates> findAllForCurrency(final CurrencyCode currency, final LocalDate date, final LocalDate from, final LocalDate to) {
        return finder.findAllForCurrencyRates(currency, date, from, to);
    }

    Collection<ExchangeRates> findLatest() {
        return finder.findAllLatest();
    }

    Optional<ExchangeRates> findLatestForCurrency(final CurrencyCode currency) {
        return finder.findLatestForCurrency(currency);
    }

}
