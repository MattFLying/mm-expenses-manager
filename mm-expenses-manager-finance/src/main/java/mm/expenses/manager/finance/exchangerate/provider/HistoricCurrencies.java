package mm.expenses.manager.finance.exchangerate.provider;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.exchangerate.exception.HistoricalCurrencyException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface for historical currencies handling with specific conditions for each provider
 *
 * @param <T> custom currency rate implementation represents single currency rate
 */
@Slf4j
@RequiredArgsConstructor
public abstract class HistoricCurrencies<T extends CurrencyRate> {

    protected final CurrencyRateProvider<T> provider;

    public abstract Collection<T> fetchHistoricalCurrencies() throws HistoricalCurrencyException;

    /**
     * Find and build collection of date range objects according to specific provider details of fetching data limitation.
     *
     * @param today           today date
     * @param startYear       year of the data beginning in provider
     * @param maxMothsToFetch max months to fetch data
     * @param maxDaysToFetch  max days to fetch data
     * @return collection of built dates
     */
    protected Collection<DateRange> findDates(final Instant today, final int startYear, final int maxMothsToFetch, final int maxDaysToFetch) {
        if (Objects.isNull(today)) {
            log.warn("Today date is incorrect because it cannot be null.");
            return Collections.emptyList();
        }
        final var result = new ArrayList<DateRange>();

        final var finalDateTo = DateUtils.instantToLocalDateUTC(today);
        var dateFrom = DateUtils.beginningOfTheYear(startYear);

        while (dateFrom.isBefore(finalDateTo)) {
            LocalDate dateTo;
            if (dateFrom.getYear() == finalDateTo.getYear() && dateFrom.getMonthValue() == finalDateTo.getMonthValue()) {
                dateTo = finalDateTo;
            } else {
                dateTo = dateFrom.plusMonths(maxMothsToFetch).minusDays(1);
            }

            final var daysBetween = DateUtils.daysBetween(dateFrom, dateTo);
            final var moreThanMaxDaysDaysToFetch = daysBetween > maxDaysToFetch;
            if (moreThanMaxDaysDaysToFetch) {
                dateTo = dateTo.minusDays(daysBetween - maxDaysToFetch);
            }
            if (dateTo.isAfter(finalDateTo)) {
                dateTo = finalDateTo;
            }

            final var dateRange = DateRange.builder()
                    .from(dateFrom)
                    .to(dateTo)
                    .build();
            result.add(dateRange);

            if (moreThanMaxDaysDaysToFetch) {
                dateFrom = dateFrom.plusMonths(maxMothsToFetch).minusDays(daysBetween - maxDaysToFetch);
            } else {
                dateFrom = dateFrom.plusMonths(maxMothsToFetch);
            }
        }
        return result;
    }

    /**
     * Finds missing currency rates dates.
     *
     * @param rates    rates of specific currency
     * @param dateFrom first date available in API to fetch
     * @param today    today date
     * @return set of all dates that are missing in fetched currency rates
     */
    protected Set<LocalDate> findMissingDates(final Collection<T> rates, final LocalDate dateFrom, final Instant today) {
        if (Objects.isNull(rates) || Objects.isNull(dateFrom) || Objects.isNull(today)) {
            log.warn("Rates collection or date from or today date is incorrect: rates - {}, date from - {}, today date - {}", rates, dateFrom, today);
            return Collections.emptySet();
        }

        final var presentDates = rates.stream()
                .map(CurrencyRate::getDate)
                .collect(Collectors.toSet());
        final var dateTo = DateUtils.instantToLocalDateUTC(today);

        final var missingDates = Stream.iterate(
                dateFrom,
                date -> date.isBefore(dateTo),
                date -> date.plusDays(1)).collect(Collectors.toCollection(TreeSet::new)
        );
        missingDates.removeAll(presentDates);

        return missingDates;
    }

    @Getter
    @Builder
    protected static class DateRange {
        private final LocalDate from;
        private final LocalDate to;
    }

}
