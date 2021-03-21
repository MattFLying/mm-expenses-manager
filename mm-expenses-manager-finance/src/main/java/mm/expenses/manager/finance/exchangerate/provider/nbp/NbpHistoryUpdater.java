package mm.expenses.manager.finance.exchangerate.provider.nbp;

import mm.expenses.manager.finance.exchangerate.provider.HistoricCurrencies;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.stream.Collectors;

class NbpHistoryUpdater extends HistoricCurrencies<NbpCurrencyRate> {

    NbpHistoryUpdater(final NbpCurrencyProvider provider) {
        super(provider);
    }

    @Override
    public Collection<NbpCurrencyRate> fetchHistoricalCurrencies() {
        final var config = provider.getProviderConfig();
        final var maxMothsToFetch = config.getDetails().getMaxMonthsToFetch();
        final var maxDaysToFetch = config.getDetails().getMaxDaysToFetch();
        final var startYear = config.getDetails().getHistoryFromYear();
        final var today = Instant.now();
        final var dates = findDates(today, startYear, maxMothsToFetch, maxDaysToFetch);

        return dates.parallelStream()
                .map(dateRange -> provider.getCurrencyRatesForDateRange(dateRange.getFrom(), dateRange.getTo()))
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(NbpCurrencyRate::getCurrency).thenComparing(NbpCurrencyRate::getDate))));
    }

}
