package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
class ExchangeRateService {

    private final ExchangeRateConfig config;
    private final ExchangeRateCommand command;
    private final ExchangeRateQuery query;
    private final ExchangeRateHistoryUpdate historyUpdate;

    void historyUpdate() {
        CompletableFuture.runAsync(historyUpdate::update);
    }

    <T extends CurrencyRate> Collection<ExchangeRate> createOrUpdate(final Collection<T> exchangeRates) {
        return command.createOrUpdate(exchangeRates);
    }

    Stream<Page<ExchangeRate>> findAll(final LocalDate date, final LocalDate from, final LocalDate to, final Pageable pageable) {
        return query.findAllCurrenciesRates(getAllRequiredCurrenciesCode(), date, from, to, pageable);
    }

    Stream<Page<ExchangeRate>> findAllForCurrency(final CurrencyCode currency, final LocalDate from, final LocalDate to, final Pageable pageable) {
        return query.findAllForCurrencyRates(currency, from, to, pageable);
    }

    Page<ExchangeRate> findLatest() {
        return query.findAllLatest(query.pageRequest(0, getAllRequiredCurrenciesCode().size()));
    }

    Optional<ExchangeRate> findLatestForCurrency(final CurrencyCode currency) {
        return query.findLatestForCurrency(currency);
    }

    Optional<ExchangeRate> findForCurrencyAndSpecificDate(final CurrencyCode currency, final LocalDate date) {
        return query.findByCurrencyAndDate(currency, date);
    }

    private Set<CurrencyCode> getAllRequiredCurrenciesCode() {
        return Stream.of(CurrencyCode.values()).filter(code -> !code.equals(CurrencyCode.UNDEFINED) || !code.equals(CurrencyCode.valueOf(config.getDefaultCurrency()))).collect(Collectors.toSet());
    }

}
