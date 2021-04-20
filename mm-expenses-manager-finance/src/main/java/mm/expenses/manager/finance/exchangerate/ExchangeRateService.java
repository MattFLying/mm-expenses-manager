package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.exchangerate.latest.UpdateLatestInMemoryEvent;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ExchangeRateService {

    private final CurrencyRatesConfig config;
    private final ExchangeRateCommand command;
    private final ExchangeRateQuery query;
    private final ExchangeRateHistoryUpdate historyUpdate;
    private final ApplicationEventPublisher eventPublisher;

    public void historyUpdate() {
        CompletableFuture.runAsync(historyUpdate::update);
    }

    public PageRequest pageRequest(final Integer pageNumber, final Integer pageSize) {
        return query.pageRequest(pageNumber, pageSize);
    }

    public Page<ExchangeRate> findByDate(final Pageable pageable, final LocalDate date) {
        return query.findByDate(pageable, DateUtils.localDateToInstant(date));
    }

    public Stream<Page<ExchangeRate>> findAll(final LocalDate date, final LocalDate from, final LocalDate to, final Pageable pageable) {
        return query.findAllCurrenciesRates(config.getAllRequiredCurrenciesCode(), date, from, to, pageable);
    }

    <T extends CurrencyRate> Collection<ExchangeRate> synchronize(final Collection<T> exchangeRates) {
        final var result = command.createOrUpdate(exchangeRates, TrailOperation.LATEST_EXCHANGE_RATES_SYNCHRONIZATION);
        eventPublisher.publishEvent(new UpdateLatestInMemoryEvent(this));
        return result;
    }

    <T extends CurrencyRate> Collection<ExchangeRate> createOrUpdate(final Collection<T> exchangeRates) {
        return command.createOrUpdate(exchangeRates, TrailOperation.CREATE_OR_UPDATE);
    }

    Stream<Page<ExchangeRate>> findAllForCurrency(final CurrencyCode currency, final LocalDate from, final LocalDate to, final Pageable pageable) {
        return query.findAllForCurrencyRates(currency, from, to, pageable);
    }

    Page<ExchangeRate> findToday() {
        return query.findAllTodayRates(query.pageRequest(0, config.getAllRequiredCurrenciesCode().size()));
    }

    Optional<ExchangeRate> findForCurrencyAndSpecificDate(final CurrencyCode currency, final LocalDate date) {
        return query.findByCurrencyAndDate(currency, date);
    }

}
