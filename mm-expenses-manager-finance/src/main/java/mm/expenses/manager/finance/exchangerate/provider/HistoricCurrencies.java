package mm.expenses.manager.finance.exchangerate.provider;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mm.expenses.manager.common.util.DateUtils.ZONE_UTC;

/**
 * Interface for historical currencies handling with specific conditions for each provider
 *
 * @param <T> custom currency rate implementation represents single currency rate
 */
@RequiredArgsConstructor
public abstract class HistoricCurrencies<T extends CurrencyRate> {

    protected final CurrencyRateProvider<T> provider;

    public abstract Collection<T> fetchHistoricalCurrencies();

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
        final var result = new ArrayList<DateRange>();

        final var finalDateTo = LocalDate.ofInstant(today, ZONE_UTC);
        var dateFrom = LocalDate.of(startYear, 1, 1);

        while (dateFrom.isBefore(finalDateTo)) {
            LocalDate dateTo;
            if (dateFrom.getYear() == finalDateTo.getYear() && dateFrom.getMonthValue() == finalDateTo.getMonthValue()) {
                dateTo = finalDateTo;
            } else {
                dateTo = dateFrom.plusMonths(maxMothsToFetch).minusDays(1);
            }

            final var daysBetween = ChronoUnit.DAYS.between(dateFrom, dateTo);
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
        final var presentDates = rates.stream()
                .map(CurrencyRate::getDate)
                .collect(Collectors.toSet());
        final var dateTo = LocalDate.ofInstant(today, ZONE_UTC);

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
