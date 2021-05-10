package mm.expenses.manager.finance.exchangerate.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.exception.HistoricalCurrencyException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface for historical currencies handling with specific conditions for each provider
 *
 * @param <T> custom currency rate implementation represents single currency rate
 */
@RequiredArgsConstructor
public abstract class HistoricCurrencies<T extends CurrencyRate> {

    protected final CurrencyRateProvider<T> provider;

    public abstract Collection<T> fetchHistoricalCurrencies() throws HistoricalCurrencyException;

    /**
     * Find and build collection of date range objects according to specific provider details of fetching data limitation.
     *
     * @param endDate        the last date
     * @param startYear      year of the data beginning in provider
     * @param maxDaysToFetch max days to fetch data
     * @return collection of built dates
     */
    protected Collection<DateRange> findDates(final Instant endDate, final int startYear, final int maxDaysToFetch) {
        final var result = new LinkedList<DateRange>();

        final var dateFrom = DateUtils.beginningOfTheYear(startYear);
        final var dateTo = DateUtils.instantToLocalDateUTC(endDate);

        if (DateUtils.daysBetween(dateFrom, dateTo) > maxDaysToFetch) {
            var from = LocalDate.from(dateFrom);
            var to = from.plusDays(maxDaysToFetch);

            while (from.isBefore(dateTo)) {
                to = from.plusDays(maxDaysToFetch);
                if (to.isAfter(dateTo)) {
                    to = LocalDate.from(dateTo);
                }

                result.add(new DateRange(from, to));
                from = prepareNewDateFrom(result);
            }
        } else {
            result.add(new DateRange(dateFrom, dateTo));
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
        final var dateTo = DateUtils.instantToLocalDateUTC(today);

        final var missingDates = Stream.iterate(
                dateFrom,
                date -> date.isBefore(dateTo),
                date -> date.plusDays(1)
        ).collect(Collectors.toCollection(TreeSet::new));

        missingDates.removeAll(presentDates);

        return missingDates;
    }

    /**
     * Prepare the new start date of new date range object. It takes last date range object from collection
     * and use its dateTo as the new value of dateFrom and add 1 extra day to avoid overlapping dates.
     */
    private LocalDate prepareNewDateFrom(final LinkedList<DateRange> list) {
        return list.getLast().getTo().plusDays(1);
    }

    @Getter
    @RequiredArgsConstructor
    protected static class DateRange {
        private final LocalDate from;
        private final LocalDate to;
    }

}
