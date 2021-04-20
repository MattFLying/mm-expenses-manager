package mm.expenses.manager.finance.exchangerate.latest;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
abstract class LatestCacheInit implements LatestRatesCache {

    private static final long DAYS_TO_FIND_IN_PAST = 10L;

    private final CurrencyRatesConfig config;
    protected final ExchangeRateService service;

    protected Set<CurrencyCode> getAllRequiredCurrenciesCode() {
        return config.getAllRequiredCurrenciesCode();
    }

    protected NavigableMap<Instant, List<ExchangeRate>> findLatestAvailableByDate() {
        final var requiredCurrencies = getAllRequiredCurrenciesCode();
        final var pageRequest = service.pageRequest(0, requiredCurrencies.size());
        NavigableMap<Instant, List<ExchangeRate>> allLatest = findAllLatest(pageRequest);

        final var now = DateUtils.instantToLocalDateUTC(DateUtils.now());
        var from = now.minusDays(DAYS_TO_FIND_IN_PAST);
        var to = LocalDate.from(now);
        var notFoundStepCount = 0L;

        while (allLatest.isEmpty()) {
            final var minusDays = notFoundStepCount * DAYS_TO_FIND_IN_PAST;
            allLatest = findAll(pageRequest, from, to, minusDays);
            notFoundStepCount++;
        }
        return allLatest;
    }

    private TreeMap<Instant, List<ExchangeRate>> findAll(final PageRequest pageRequest, final LocalDate from, final LocalDate to, final long minusDays) {
        return service.findAll(null, from.minusDays(minusDays), to.minusDays(minusDays), pageRequest)
                .map(Slice::getContent)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(ExchangeRate::getDate, TreeMap::new, Collectors.toList()));
    }

    private TreeMap<Instant, List<ExchangeRate>> findAllLatest(final PageRequest pageRequest) {
        return service.findByDate(pageRequest, DateUtils.instantToLocalDateUTC(Instant.now()))
                .stream()
                .collect(Collectors.groupingBy(ExchangeRate::getDate, TreeMap::new, Collectors.toList()));
    }

}
