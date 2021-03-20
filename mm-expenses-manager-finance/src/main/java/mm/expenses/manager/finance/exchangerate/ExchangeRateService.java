package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
class ExchangeRateService {

    private final CurrencyRateProvider<? extends CurrencyRate> provider;
    private final ExchangeRateCommand command;
    private final ExchangeRateQuery query;

    void historyUpdate() {
        Executors.newSingleThreadExecutor().execute(() -> {
            log.info("Currencies history update in progress.");
            command.saveHistory(provider.getAllHistoricalCurrencies());
            log.info("Currencies history update has been done.");
        });
    }

    void saveAllCurrent() {
        final var allCurrent = provider.getCurrentCurrencyRates();
        if (!allCurrent.isEmpty()) {
            command.createOrUpdate(allCurrent);
        }
    }

    Stream<ExchangeRate> findAll(final LocalDate date, final LocalDate from, final LocalDate to) {
        return query.findAllCurrenciesRates(date, from, to);
    }

    Stream<ExchangeRate> findAllForCurrency(final CurrencyCode currency, final LocalDate date, final LocalDate from, final LocalDate to) {
        return query.findAllForCurrencyRates(currency, date, from, to);
    }

    Stream<ExchangeRate> findLatest() {
        return query.findAllLatest();
    }

    Optional<ExchangeRate> findLatestForCurrency(final CurrencyCode currency) {
        return query.findLatestForCurrency(currency);
    }

}
