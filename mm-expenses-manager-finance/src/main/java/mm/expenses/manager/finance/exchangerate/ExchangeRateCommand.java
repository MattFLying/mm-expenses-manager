package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.DefaultCurrencyProvider;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Slf4j
@Component
@RequiredArgsConstructor
class ExchangeRateCommand {

    private final ExchangeRateRepository repository;
    private final ExchangeRateMapper mapper;
    private final DefaultCurrencyProvider<?> provider;

    /**
     * Save or update historical exchange rates.
     *
     * @param historicData historical data that should be saved or updated
     * @param <T>          specific type of CurrencyRate depends of current provider
     */
    <T extends CurrencyRate> void saveHistory(final Collection<T> historicData) {
        final var historicalByCurrency = historicData.stream().collect(Collectors.groupingBy(CurrencyRate::getCurrency, toList()));
        final var existedByCurrency = repository.findAll().stream().collect(Collectors.groupingBy(ExchangeRateEntity::getCurrency, toList()));

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
        final var existedByCurrency = findByCurrenciesAndDateOrDateRange(currencies, fromDate, toDate).stream().collect(Collectors.groupingBy(ExchangeRateEntity::getCurrency, toList()));

        final var toBeSaved = createOrUpdate(toSaveByCurrency, existedByCurrency);
        final var savedHistory = repository.saveAll(toBeSaved);
        final var savedHistoryCount = savedHistory.size();
        final var duplicatesCount = exchangeRates.size() - savedHistoryCount;

        log.info("{} currencies saved, {} duplicates skipped.", savedHistoryCount, duplicatesCount);
        return savedHistory.stream()
                .map(mapper::mapFromEntity)
                .collect(toList());
    }

    /**
     * Save or update exchange rates in comparison already existed data with the new data set.
     *
     * @param exchangeRatesByCurrency data that should be saved or updated
     * @param existedByCurrency       data that already exist
     * @param <T>                     specific type of CurrencyRate depends of current provider
     * @return collection of saved or updated exchange rate objects
     */
    private <T extends CurrencyRate> List<ExchangeRateEntity> createOrUpdate(final Map<CurrencyCode, List<T>> exchangeRatesByCurrency, final Map<CurrencyCode, List<ExchangeRateEntity>> existedByCurrency) {
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
                                .map(mapper::mapToEntity)
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
    private <T extends CurrencyRate> Optional<ExchangeRateEntity> createOrUpdate(final Map<LocalDate, ExchangeRateEntity> existedByDate, final T currencyRate) {
        final var providerName = provider.getName();
        final var existedOpt = Optional.ofNullable(existedByDate.get(currencyRate.getDate()));
        if (existedOpt.isPresent()) {
            final var existed = existedOpt.get();
            if (!existed.hasProvider(providerName)) {
                existed.addRateForProvider(providerName, currencyRate.getRate());
                existed.addDetailsForProvider(providerName, currencyRate.getDetails());
                return Optional.of(existed);
            }
            return Optional.empty();
        }
        return Optional.of(mapper.mapToEntity(currencyRate));
    }

    /**
     * Find all exchange rates of given currencies for specific date or date range.
     *
     * @param currencies currencies to search
     * @param fromDate   date from
     * @param toDate     date to
     * @return all exchange rates that fulfil ther criteria
     */
    private Collection<ExchangeRateEntity> findByCurrenciesAndDateOrDateRange(final Set<CurrencyCode> currencies, final LocalDate fromDate, final LocalDate toDate) {
        if (!fromDate.isEqual(toDate)) {
            return repository.findByCurrencyInAndDateBetween(currencies, mapper.fromLocalDateToInstant(fromDate).minus(1, ChronoUnit.DAYS), mapper.fromLocalDateToInstant(toDate).plus(1, ChronoUnit.DAYS));
        }
        return repository.findByCurrencyInAndDate(currencies, mapper.fromLocalDateToInstant(fromDate));
    }

    private <T extends CurrencyRate> LocalDate findDateTo(final Collection<T> currencyRates) {
        return currencyRates.stream().max(Comparator.comparing(CurrencyRate::getDate)).map(CurrencyRate::getDate).orElseThrow(() -> new IllegalArgumentException("Invalid data, could not find date from"));
    }

    private <T extends CurrencyRate> LocalDate findDateFrom(final Collection<T> currencyRates) {
        return currencyRates.stream().min(Comparator.comparing(CurrencyRate::getDate)).map(CurrencyRate::getDate).orElseThrow(() -> new IllegalArgumentException("Invalid data, could not find date to"));
    }










    /*
    private Collection<ExchangeRateEntity> findByDateOrDateRange(final CurrencyCode currency, final LocalDate fromDate, final LocalDate toDate) {
        if (!fromDate.isEqual(toDate)) {
            return repository.findByCurrencyAndDateBetween(currency, mapper.fromLocalDateToInstant(fromDate).minus(1, ChronoUnit.DAYS), mapper.fromLocalDateToInstant(toDate).plus(1, ChronoUnit.DAYS));
        }
        return repository.findByCurrencyAndDate(currency, mapper.fromLocalDateToInstant(fromDate)).map(List::of).orElse(Collections.emptyList());
    }


    <T extends CurrencyRate> Optional<ExchangeRate> create(final T currencyRate) {
        return repository.findByCurrencyAndDate(currencyRate.getCurrency(), mapper.fromLocalDateToInstant(currencyRate.getDate()))
                .map(rateEntity -> {
                    final var exchangeRate = mapper.mapFromEntity(rateEntity);
                    if (exchangeRate.hasProvider(provider.getName())) {
                        log.info("Currency {} for date {} already exists", currencyRate.getCurrency(), currencyRate.getDate());
                        return Optional.ofNullable(mapper.mapFromEntity(rateEntity));
                    } else {
                        log.info("Currency {} for date {} already exists but without current provider. Will be updated.", currencyRate.getCurrency(), currencyRate.getDate());
                        exchangeRate.addRateForProvider(provider.getName(), currencyRate.getRate());
                        exchangeRate.addDetailsForProvider(provider.getName(), currencyRate.getDetails());

                        final var updated = repository.save(mapper.mapToEntityFromDomain(exchangeRate));
                        return Optional.ofNullable(mapper.mapFromEntity(updated));
                    }
                }).orElseGet(() -> {
                    final var saved = repository.save(mapper.mapToEntity(currencyRate));
                    return Optional.ofNullable(mapper.mapFromEntity(saved));
                });
    }

    <T extends CurrencyRate> Collection<ExchangeRate> createForDateRange(final CurrencyCode currency, final Collection<T> forDateRange) {
        final var fromDate = findDateFrom(forDateRange);
        final var toDate = findDateTo(forDateRange);
        final var currenciesByName = forDateRange.stream().collect(Collectors.groupingBy(CurrencyRate::getCurrency, toList()));

        final var existed = findByDateOrDateRange(currency, fromDate, toDate).stream()
                .map(rateEntity -> updateIfCurrentProviderDoesNotExistsAndSaveChanges(currenciesByName, rateEntity))
                .collect(Collectors.toMap(
                        ExchangeRate::getDate,
                        Function.identity()
                ));

        final var notExisted = forDateRange.stream()
                .filter(toSave -> !existed.containsKey(toSave.getDate()))
                .map(toSave -> mapper.mapToEntity(toSave))
                .map(repository::save)
                .map(mapper::mapFromEntity)
                .collect(Collectors.toMap(
                        ExchangeRate::getDate,
                        Function.identity()
                ));

        log.info("{} currencies exist and won't be saved, {} new saved", existed.size(), notExisted.size());
        return Stream.concat(existed.values().stream(), notExisted.values().stream()).collect(toList());
    }

    <T extends CurrencyRate> Collection<ExchangeRate> createAllForDateRange(final Collection<T> allForDateRange) {
        final var currencies = allForDateRange.stream().map(CurrencyRate::getCurrency).collect(Collectors.toSet());
        final var currenciesByName = allForDateRange.stream().collect(Collectors.groupingBy(CurrencyRate::getCurrency, toList()));
        final var fromDate = findDateFrom(allForDateRange);
        final var toDate = findDateTo(allForDateRange);

        final var existedByCurrency = findByDateOrDateRange(currencies, fromDate, toDate).stream()
                .map(rateEntity -> updateIfCurrentProviderDoesNotExistsAndSaveChanges(currenciesByName, rateEntity))
                .collect(Collectors.groupingBy(ExchangeRate::getCurrency, Collectors.groupingBy(ExchangeRate::getDate)));

        final var notExisted = allForDateRange.stream()
                .collect(Collectors.groupingBy(
                        CurrencyRate::getCurrency,
                        Collectors.groupingBy(CurrencyRate::getDate)
                ))
                .entrySet()
                .stream()
                .map(currencyRate -> mapMissingCurrencies(existedByCurrency, currencyRate))
                .map(Map::values)
                .map(groupedByCurrency -> groupedByCurrency.stream()
                        .map(Map::values)
                        .flatMap(Collection::stream)
                        .flatMap(Collection::stream)
                        .collect(toList())
                )
                .flatMap(Collection::stream)
                .map(repository::save)
                .map(mapper::mapFromEntity)
                .collect(toList());

        final var existed = existedByCurrency.values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(toList());

        log.info("{} currencies exist and won't be saved, {} new saved", existed.size(), notExisted.size());
        return Stream.concat(existed.stream(), notExisted.stream()).collect(toList());
    }

    private <T extends CurrencyRate> Map<CurrencyCode, Map<LocalDate, List<ExchangeRateEntity>>> mapMissingDates(final Map<CurrencyCode, Map<LocalDate, List<ExchangeRate>>> existed, final CurrencyCode currencyCode, final Map<LocalDate, List<T>> groupedByDate) {
        final var existedGroupedByDate = existed.get(currencyCode);
        return groupedByDate.entrySet()
                .stream()
                .filter(currencyRate -> !existedGroupedByDate.containsKey(currencyRate.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .map(toSave -> mapper.mapToEntity(toSave))
                .collect(Collectors.groupingBy(ExchangeRateEntity::getCurrency, Collectors.groupingBy(entity -> mapper.fromInstantToLocalDate(entity.getDate()))));
    }

    private <T extends CurrencyRate> Map<CurrencyCode, Map<LocalDate, List<ExchangeRateEntity>>> mapMissingCurrencies(final Map<CurrencyCode, Map<LocalDate, List<ExchangeRate>>> existed, final Map.Entry<CurrencyCode, Map<LocalDate, List<T>>> currencyRate) {
        final var currencyCode = currencyRate.getKey();
        final var groupedByDate = currencyRate.getValue();

        return existed.getOrDefault(currencyCode, Collections.emptyMap()).isEmpty()
                ? groupedByDate.values()
                .stream()
                .flatMap(Collection::stream)
                .map(toSave -> mapper.mapToEntity(toSave))
                .collect(Collectors.groupingBy(ExchangeRateEntity::getCurrency, Collectors.groupingBy(entity -> mapper.fromInstantToLocalDate(entity.getDate()))))
                : mapMissingDates(existed, currencyCode, groupedByDate);
    }*/

}
