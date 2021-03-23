package mm.expenses.manager.finance.exchangerate.provider.nbp;

import mm.expenses.manager.common.util.MergeUtils;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.HistoricCurrencies;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static mm.expenses.manager.finance.exchangerate.provider.CurrencyRate.currencyRateComparator;

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
        final var dateFrom = LocalDate.of(startYear, 1, 1);
        final var dates = findDates(today, startYear, maxMothsToFetch, maxDaysToFetch);

        final var fetchedRates = dates.stream()
                .map(dateRange -> provider.getCurrencyRatesForDateRange(dateRange.getFrom(), dateRange.getTo()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        final var missingDates = findMissingDates(fetchedRates, dateFrom, today);

        final var missingRates = fetchedRates.stream()
                .collect(Collectors.groupingBy(
                        CurrencyRate::getCurrency,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                result -> result.stream().collect(Collectors.toMap(NbpCurrencyRate::getDate, Function.identity(), MergeUtils::firstWins, LinkedHashMap::new)))
                ))
                .values()
                .stream()
                .map(ratesForCurrencyByDate -> missingDates.stream()
                        .filter(missingDate -> !ratesForCurrencyByDate.containsKey(missingDate))
                        .map(missingDate -> prepareMissingCurrency(dateFrom, missingDate, ratesForCurrencyByDate))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet())
                )
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        fetchedRates.addAll(missingRates);

        return fetchedRates.stream().collect(Collectors.toCollection(() -> new TreeSet<>(currencyRateComparator())));
    }

    /**
     * Regarding to missing dates finds the previous available currency rate and use same values for the missing date.
     * e.g. if 2020-01-02 is missing and 2020-01-01 exists then currency for 2020-01-02 date will be the same as for 2020-01-01
     *
     * @param dateFrom               first date available in API to fetch
     * @param missingDate            missing date for currency rate
     * @param ratesForCurrencyByDate rates for currency by date
     * @return
     */
    private Optional<NbpCurrencyRate> prepareMissingCurrency(final LocalDate dateFrom, final LocalDate missingDate, final LinkedHashMap<LocalDate, NbpCurrencyRate> ratesForCurrencyByDate) {
        var exists = false;
        var previousDateToFind = missingDate.minusDays(1);
        NbpCurrencyRate result = null;
        while (!exists) {
            if (isDateEqualOrBeforeTheBeginningDate(dateFrom, previousDateToFind)) {
                break;
            }

            if (ratesForCurrencyByDate.containsKey(previousDateToFind)) {
                exists = true;
                result = NbpCurrencyRate.sameDataDifferentDate(ratesForCurrencyByDate.get(previousDateToFind), missingDate);
            } else {
                previousDateToFind = previousDateToFind.minusDays(1);
            }
        }
        return Optional.ofNullable(result);
    }

    private boolean isDateEqualOrBeforeTheBeginningDate(final LocalDate dateFrom, final LocalDate previousDateToFind) {
        return previousDateToFind.isEqual(dateFrom) || previousDateToFind.isBefore(dateFrom);
    }

}
