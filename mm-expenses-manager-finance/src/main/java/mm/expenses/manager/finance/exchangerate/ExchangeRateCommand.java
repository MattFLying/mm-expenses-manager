package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@Slf4j
@Component
@RequiredArgsConstructor
class ExchangeRateCommand {

    private final ExchangeRateRepository repository;
    private final ExchangeRateMapper mapper;
    private final CurrencyProviders providers;

    /**
     * Save or update historical exchange rates.
     *
     * @param historicData historical data that should be saved or updated
     * @param <T>          specific type of CurrencyRate depends of current provider
     */
    <T extends CurrencyRate> void saveHistory(final Collection<T> historicData) {
        final var historicalByCurrency = historicData.stream().collect(Collectors.groupingBy(CurrencyRate::getCurrency, toList()));
        final var existedByCurrency = repository.findAll().stream().collect(Collectors.groupingBy(ExchangeRate::getCurrency, toList()));

        final var toBeSaved = createOrUpdate(historicalByCurrency, existedByCurrency);
        final var savedHistoryCount = repository.saveAll(toBeSaved).size();
        final var duplicatesCount = historicData.size() - savedHistoryCount;

        log.info("{} historical currencies saved, {} duplicates skipped.", savedHistoryCount, duplicatesCount);
    }

    /**
     * Save or update exchange rates from passed collected data.
     *
     * @param exchangeRates data that should be saved or updated
     * @param <T>           specific type of CurrencyRate depends of current provider
     * @return collection of saved or updated exchange rate objects
     */
    <T extends CurrencyRate> Collection<ExchangeRate> createOrUpdate(final Collection<T> exchangeRates) {
        final var currencies = exchangeRates.stream().map(CurrencyRate::getCurrency).collect(Collectors.toSet());
        final var toSaveByCurrency = exchangeRates.stream().collect(Collectors.groupingBy(CurrencyRate::getCurrency, toList()));

        final var fromDate = findDateFrom(exchangeRates);
        final var toDate = findDateTo(exchangeRates);
        final var existedByCurrency = findByCurrenciesAndDateOrDateRange(currencies, fromDate, toDate).collect(
                Collectors.groupingBy(ExchangeRate::getCurrency, toList())
        );

        final var toBeSaved = createOrUpdate(toSaveByCurrency, existedByCurrency);
        final var savedHistory = repository.saveAll(toBeSaved);
        final var savedHistoryCount = savedHistory.size();
        final var duplicatesCount = exchangeRates.size() - savedHistoryCount;

        log.info("{} currencies saved, {} duplicates skipped.", savedHistoryCount, duplicatesCount);
        return savedHistory;
    }

    /**
     * Save or update exchange rates in comparison already existed data with the new data set.
     *
     * @param exchangeRatesByCurrency data that should be saved or updated
     * @param existedByCurrency       data that already exist
     * @param <T>                     specific type of CurrencyRate depends of current provider
     * @return collection of saved or updated exchange rate objects
     */
    private <T extends CurrencyRate> List<ExchangeRate> createOrUpdate(final Map<CurrencyCode, List<T>> exchangeRatesByCurrency, final Map<CurrencyCode, List<ExchangeRate>> existedByCurrency) {
        return exchangeRatesByCurrency.entrySet()
                .stream()
                .map(rateEntry -> {
                    if (existedByCurrency.containsKey(rateEntry.getKey())) {
                        final var rates = existedByCurrency.getOrDefault(rateEntry.getKey(), Collections.emptyList())
                                .stream()
                                .collect(Collectors.toMap(entity -> mapper.fromInstantToLocalDate(entity.getDate()), Function.identity()));

                        return rateEntry.getValue().stream()
                                .map(rate -> createOrUpdate(rates, rate))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList());
                    } else {
                        return rateEntry.getValue().stream()
                                .map(rate -> mapper.map(rate, mapper.createInstantNow()))
                                .collect(Collectors.toList());
                    }
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Save or update exchange rate.
     *
     * @param existedByDate data that already exist grouped by date
     * @param currencyRate  currency rate of specific provider that should be saved or updated
     * @param <T>           specific type of CurrencyRate depends of current provider
     * @return saved or updated exchange rate object or empty optional if there is no provider to be added
     */
    private <T extends CurrencyRate> Optional<ExchangeRate> createOrUpdate(final Map<LocalDate, ExchangeRate> existedByDate, final T currencyRate) {
        final var providerName = providers.getDefaultProvider();
        final var targetCurrency = providers.getDefaultCurrency();
        final var existedOpt = Optional.ofNullable(existedByDate.get(currencyRate.getDate()));
        if (existedOpt.isPresent()) {
            final var existed = existedOpt.get();
            if (!existed.hasProvider(providerName)) {
                final var rate = mapper.map(existed.getCurrency(), targetCurrency, currencyRate.getRate());
                existed.addRateForProvider(providerName, rate);
                existed.addDetailsForProvider(providerName, currencyRate.getDetails());
                return Optional.of(ExchangeRate.modified(existed, mapper.createInstantNow()));
            }
            return Optional.empty();
        }
        return Optional.of(mapper.map(currencyRate, mapper.createInstantNow()));
    }

    /**
     * Find all exchange rates of given currencies for specific date or date range.
     *
     * @param currencies currencies to search
     * @param fromDate   date from
     * @param toDate     date to
     * @return all exchange rates that fulfil ther criteria
     */
    private Stream<ExchangeRate> findByCurrenciesAndDateOrDateRange(final Set<CurrencyCode> currencies, final LocalDate fromDate, final LocalDate toDate) {
        if (!fromDate.isEqual(toDate)) {
            return repository.findByCurrencyInAndDateBetween(currencies, mapper.fromLocalDateToInstant(fromDate).minus(1, ChronoUnit.DAYS), mapper.fromLocalDateToInstant(toDate).plus(1, ChronoUnit.DAYS));
        }
        return repository.findByCurrencyInAndDate(currencies, mapper.fromLocalDateToInstant(fromDate));
    }

    /**
     * Find maximum date to.
     *
     * @param currencyRates currency rate data objects
     * @param <T>
     * @return maximum date to
     */
    private <T extends CurrencyRate> LocalDate findDateTo(final Collection<T> currencyRates) {
        return currencyRates.stream().max(Comparator.comparing(CurrencyRate::getDate)).map(CurrencyRate::getDate).orElseThrow(() -> new IllegalArgumentException("Invalid data, could not find date from"));
    }

    /**
     * Find minimum date from.
     *
     * @param currencyRates currency rate data objects
     * @param <T>           specific type of CurrencyRate depends of current provider
     * @return minimum date from
     */
    private <T extends CurrencyRate> LocalDate findDateFrom(final Collection<T> currencyRates) {
        return currencyRates.stream().min(Comparator.comparing(CurrencyRate::getDate)).map(CurrencyRate::getDate).orElseThrow(() -> new IllegalArgumentException("Invalid data, could not find date to"));
    }

}
