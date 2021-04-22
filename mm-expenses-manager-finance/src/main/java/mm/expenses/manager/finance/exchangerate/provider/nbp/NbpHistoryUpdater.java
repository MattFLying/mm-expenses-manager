package mm.expenses.manager.finance.exchangerate.provider.nbp;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.common.util.MergeUtils;
import mm.expenses.manager.finance.exchangerate.exception.CurrencyProviderException;
import mm.expenses.manager.finance.exchangerate.exception.FinanceExceptionMessage;
import mm.expenses.manager.finance.exchangerate.exception.HistoricalCurrencyException;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.HistoricCurrencies;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static mm.expenses.manager.finance.exchangerate.provider.CurrencyRate.currencyRateComparator;

@Slf4j
class NbpHistoryUpdater extends HistoricCurrencies<NbpCurrencyRate> {

    NbpHistoryUpdater(final NbpCurrencyProvider provider) {
        super(provider);
    }

    @Override
    public Collection<NbpCurrencyRate> fetchHistoricalCurrencies() throws HistoricalCurrencyException {
        try {
            final var config = provider.getProviderConfig();
            final var maxDaysToFetch = config.getDetails().getMaxDaysToFetch();
            final var startYear = config.getDetails().getHistoryFromYear();
            final var today = DateUtils.now();
            final var dateFrom = DateUtils.beginningOfTheYear(startYear);
            final var dates = findDates(today, startYear, maxDaysToFetch);

            final var fetchedRates = dates.stream()
                    .map(dateRange -> {
                        try {
                            return provider.getCurrencyRatesForDateRange(dateRange.getFrom(), dateRange.getTo());
                        } catch (final CurrencyProviderException exception) {
                            log.warn("Cannot fetch historical currency rates for dates between: {} - {}.", dateRange.getFrom(), dateRange.getTo(), exception);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            final var missingDates = findMissingDates(fetchedRates, dateFrom, today);

            final var missingRates = fetchedRates.stream()
                    .collect(Collectors.groupingBy(
                            CurrencyRate::getCurrency,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    result -> result.stream().collect(
                                            Collectors.toMap(
                                                    NbpCurrencyRate::getDate,
                                                    Function.identity(),
                                                    MergeUtils::firstWins,
                                                    LinkedHashMap::new
                                            )
                                    )
                            )
                    ))
                    .values()
                    .stream()
                    .map(ratesForCurrencyByDate -> missingDates.stream()
                            .filter(missingDate -> !ratesForCurrencyByDate.containsKey(missingDate))
                            .map(missingDate -> prepareMissingCurrency(dateFrom, missingDate, DateUtils.instantToLocalDateUTC(today), ratesForCurrencyByDate))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toSet())
                    )
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            fetchedRates.addAll(missingRates);

            return fetchedRates.stream().collect(Collectors.toCollection(() -> new TreeSet<>(currencyRateComparator())));
        } catch (final Exception exception) {
            throw new HistoricalCurrencyException(FinanceExceptionMessage.SAVE_HISTORIC_EXCHANGE_RATES_UNKNOWN_ERROR, exception);
        }
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
    private Optional<NbpCurrencyRate> prepareMissingCurrency(final LocalDate dateFrom, final LocalDate missingDate, final LocalDate today, final LinkedHashMap<LocalDate, NbpCurrencyRate> ratesForCurrencyByDate) {
        final var dayToRemove = 1;
        var exists = false;
        var previousDateToFind = missingDate.minusDays(dayToRemove);
        NbpCurrencyRate result = null;

        while (!exists) {
            if (isDateEqualOrBeforeTheBeginningDate(dateFrom, previousDateToFind) || missingDate.isEqual(today)) {
                break;
            }

            if (ratesForCurrencyByDate.containsKey(previousDateToFind)) {
                exists = true;
                result = NbpCurrencyRate.sameDataDifferentDate(ratesForCurrencyByDate.get(previousDateToFind), missingDate);
            } else {
                previousDateToFind = previousDateToFind.minusDays(dayToRemove);
            }
        }
        return Optional.ofNullable(result);
    }

    private boolean isDateEqualOrBeforeTheBeginningDate(final LocalDate dateFrom, final LocalDate previousDateToFind) {
        return previousDateToFind.isEqual(dateFrom) || previousDateToFind.isBefore(dateFrom);
    }

}
