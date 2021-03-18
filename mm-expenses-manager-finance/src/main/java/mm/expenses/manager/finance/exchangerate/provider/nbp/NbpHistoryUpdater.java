package mm.expenses.manager.finance.exchangerate.provider.nbp;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.finance.exchangerate.provider.HistoricCurrencies;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class NbpHistoryUpdater implements HistoricCurrencies<NbpCurrencyRate> {

    private final NbpApiConfig nbpApiConfig;
    private final NbpCurrencyProvider provider;

    @Override
    public Collection<NbpCurrencyRate> fetchHistoricalCurrencies() {
        final var maxMothsToFetch = nbpApiConfig.getDetails().getMaxMonthsToFetch();
        final var maxDaysToFetch = nbpApiConfig.getDetails().getMaxDaysToFetch();
        final var startYear = nbpApiConfig.getDetails().getHistoryFromYear();
        final var today = Instant.now();
        final var dates = findDates(today, startYear, maxMothsToFetch, maxDaysToFetch);

        return dates.stream()
                .map(dateRange -> provider.getCurrencyRatesForDateRange(dateRange.getFrom(), dateRange.getTo()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Collection<DateRange> findDates(final Instant today, final int startYear, final int maxMothsToFetch, final int maxDaysToFetch) {
        final var result = new ArrayList<DateRange>();

        final var finalDateTo = LocalDate.ofInstant(today, ZoneId.of("UTC"));
        var dateFrom = LocalDate.of(startYear, 1, 1);

        while (dateFrom.isBefore(finalDateTo)) {
            LocalDate dateTo;
            if (dateFrom.getYear() == finalDateTo.getYear() && dateFrom.getMonthValue() == finalDateTo.getMonthValue()) {
                dateTo = finalDateTo;
            } else {
                dateTo = dateFrom.plusMonths(maxMothsToFetch).minusDays(1);
            }

            final var daysBetween = ChronoUnit.DAYS.between(dateFrom, dateTo);
            final var moreThan90Days = daysBetween > maxDaysToFetch;
            if (moreThan90Days) {
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

            if (moreThan90Days) {
                dateFrom = dateFrom.plusMonths(maxMothsToFetch).minusDays(daysBetween - maxDaysToFetch);
            } else {
                dateFrom = dateFrom.plusMonths(maxMothsToFetch);
            }
        }
        return result;
    }

    @Getter
    @Builder
    private static class DateRange {
        private final LocalDate from;
        private final LocalDate to;
    }

}
