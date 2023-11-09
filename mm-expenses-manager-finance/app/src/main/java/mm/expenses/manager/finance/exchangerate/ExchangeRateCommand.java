package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.exception.common.InvalidDateException;
import mm.expenses.manager.finance.exception.ExchangeRateException;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.trail.ExchangeRateTrailService;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation.State;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
class ExchangeRateCommand {

    private final ExchangeRateRepository repository;
    private final ExchangeRateMapper mapper;
    private final CurrencyProviders providers;
    private final ExchangeRateTrailService trails;

    /**
     * Save or update historical exchange rates.
     *
     * @param historicData historical data that should be saved or updated
     * @param <T>          specific type of CurrencyRate depends of current provider
     */
    <T extends CurrencyRate> void saveHistory(final Collection<T> historicData) {
        final List<ExchangeRate> savedHistory = new ArrayList<>();
        var savedHistoryCount = 0;
        var duplicatesCount = 0;
        var status = State.ERROR;
        try {
            final var historicalByCurrency = historicData.stream().collect(Collectors.groupingBy(CurrencyRate::getCurrency, toList()));
            final var existedByCurrency = repository.findAll().stream().collect(Collectors.groupingBy(ExchangeRate::getCurrency, toList()));

            final var toBeSaved = createOrUpdate(historicalByCurrency, existedByCurrency);
            savedHistory.addAll(repository.saveAll(toBeSaved));
            savedHistoryCount = savedHistory.size();
            duplicatesCount = historicData.size() - savedHistoryCount;
            status = State.SUCCESS;
        } catch (final Exception exception) {
            throw new ExchangeRateException(FinanceExceptionMessage.SAVE_HISTORIC_EXCHANGE_RATES, exception);
        } finally {
            trails.saveLog(TrailOperation.EXCHANGE_RATES_HISTORY_UPDATE.withStatus(status), savedHistory.stream().map(ExchangeRate::getId).collect(toList()), savedHistoryCount, duplicatesCount);
        }
    }

    /**
     * Save or update exchange rates from passed collected data.
     *
     * @param exchangeRates data that should be saved or updated
     * @param operation     type of operation executed via this method
     * @param <T>           specific type of CurrencyRate depends of current provider
     * @return collection of saved or updated exchange rate objects
     */
    <T extends CurrencyRate> Collection<ExchangeRate> createOrUpdate(final Collection<T> exchangeRates, final TrailOperation operation) {
        final List<ExchangeRate> savedHistory = new ArrayList<>();
        var savedHistoryCount = 0;
        var duplicatesCount = 0;
        var status = State.ERROR;
        try {
            final var currencies = exchangeRates.stream().map(CurrencyRate::getCurrency).collect(Collectors.toSet());
            final var toSaveByCurrency = exchangeRates.stream().collect(Collectors.groupingBy(CurrencyRate::getCurrency, toList()));

            final var fromDate = findDateFrom(exchangeRates);
            final var toDate = findDateTo(exchangeRates);
            final var existedByCurrency = findByCurrenciesAndDateOrDateRange(currencies, fromDate, toDate).collect(
                    Collectors.groupingBy(ExchangeRate::getCurrency, toList())
            );

            final var toBeSaved = createOrUpdate(toSaveByCurrency, existedByCurrency);
            savedHistory.addAll(repository.saveAll(toBeSaved));
            savedHistoryCount = savedHistory.size();
            duplicatesCount = exchangeRates.size() - savedHistoryCount;
            status = State.SUCCESS;
        } catch (final InvalidDateException exception) {
            throw new ExchangeRateException(exception.getType(), exception);
        } catch (final Exception exception) {
            throw new ExchangeRateException(FinanceExceptionMessage.SAVE_OR_UPDATE_EXCHANGE_RATES, exception);
        } finally {
            trails.saveLog(operation.withStatus(status), savedHistory.stream().map(ExchangeRate::getId).collect(toList()), savedHistoryCount, duplicatesCount);
        }
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
        final var providerName = providers.getProviderName();
        final var targetCurrency = providers.getCurrency();
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
     * @param <T>           specific type of CurrencyRate depends of current provider
     * @return maximum date to
     */
    private <T extends CurrencyRate> LocalDate findDateTo(final Collection<T> currencyRates) {
        return findDateOrThrowException(() -> currencyRates.stream().max(Comparator.comparing(CurrencyRate::getDate)), FinanceExceptionMessage.EXCHANGE_RATES_INVALID_DATE_FROM);
    }

    /**
     * Find minimum date from.
     *
     * @param currencyRates currency rate data objects
     * @param <T>           specific type of CurrencyRate depends of current provider
     * @return minimum date from
     */
    private <T extends CurrencyRate> LocalDate findDateFrom(final Collection<T> currencyRates) {
        return findDateOrThrowException(() -> currencyRates.stream().min(Comparator.comparing(CurrencyRate::getDate)), FinanceExceptionMessage.EXCHANGE_RATES_INVALID_DATE_TO);
    }

    private <T extends CurrencyRate> LocalDate findDateOrThrowException(final Supplier<Optional<T>> dateExtractor, final FinanceExceptionMessage exceptionMessage) {
        try {
            return dateExtractor.get().map(CurrencyRate::getDate).orElseThrow(() -> new InvalidDateException(exceptionMessage));
        } catch (final Exception exception) {
            throw new InvalidDateException(exceptionMessage);
        }
    }

}
