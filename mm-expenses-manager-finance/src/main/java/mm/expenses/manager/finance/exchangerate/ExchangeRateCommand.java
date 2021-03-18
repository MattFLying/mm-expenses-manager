package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.DefaultCurrencyProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
class ExchangeRateCommand {

    private final ExchangeRateRepository repository;
    private final ExchangeRateMapper mapper;
    private final DefaultCurrencyProvider<?> provider;

    <T extends CurrencyRate> Optional<ExchangeRate> create(final T currencyRate) {
        return repository.findByCurrencyAndDate(currencyRate.getCurrency(), mapper.fromLocalDateToInstant(currencyRate.getDate()))
                .map(rateEntity -> {
                    final var exchangeRate = mapper.map(rateEntity);
                    if (exchangeRate.hasProvider(provider.getName())) {
                        log.info("Currency {} for date {} already exists", currencyRate.getCurrency(), currencyRate.getDate());
                        return Optional.ofNullable(mapper.map(rateEntity));
                    } else {
                        log.info("Currency {} for date {} already exists but without current provider. Will be updated.", currencyRate.getCurrency(), currencyRate.getDate());
                        exchangeRate.addRateForProvider(provider.getName(), currencyRate.getRate());
                        exchangeRate.addDetailsForProvider(provider.getName(), currencyRate.getDetails());

                        final var updated = repository.save(mapper.map(exchangeRate));
                        return Optional.ofNullable(mapper.map(updated));
                    }
                }).orElseGet(() -> {
                    final var saved = repository.save(mapper.map(currencyRate));
                    return Optional.ofNullable(mapper.map(saved));
                });
    }

    <T extends CurrencyRate> Collection<ExchangeRate> createForDateRange(final CurrencyCode currency, final Collection<T> forDateRange) {
        final var fromDate = findDateFrom(forDateRange);
        final var toDate = findDateTo(forDateRange);
        final var currenciesByName = forDateRange.stream().collect(Collectors.groupingBy(CurrencyRate::getCurrency, toList()));

        final var existed = findByDateOrDateRange(currency, fromDate, toDate).stream()
                .map(rateEntity -> updateIfCurrentProviderDoesNotExists(currenciesByName, rateEntity))
                .collect(Collectors.toMap(
                        ExchangeRate::getDate,
                        Function.identity()
                ));

        final var notExisted = forDateRange.stream()
                .filter(toSave -> !existed.containsKey(toSave.getDate()))
                .map(toSave -> mapper.map(toSave))
                .map(repository::save)
                .map(mapper::map)
                .collect(Collectors.toMap(
                        ExchangeRate::getDate,
                        Function.identity()
                ));

        log.info("{} currencies exist and won't be saved, {} new saved", existed.size(), notExisted.size());
        return Stream.concat(existed.values().stream(), notExisted.values().stream()).collect(toList());
    }

    <T extends CurrencyRate> Collection<ExchangeRate> createAll(final Collection<T> all) {
        final var currencies = all.stream().map(CurrencyRate::getCurrency).collect(Collectors.toSet());
        final var currenciesByName = all.stream().collect(Collectors.groupingBy(CurrencyRate::getCurrency, toList()));
        final var fromDate = findDateFrom(all);
        final var toDate = findDateTo(all);

        final var existed = findByDateOrDateRange(currencies, fromDate, toDate).stream()
                .map(rateEntity -> updateIfCurrentProviderDoesNotExists(currenciesByName, rateEntity))
                .collect(Collectors.toMap(
                        ExchangeRate::getCurrency,
                        Function.identity(),
                        this::getTheNewest
                ));

        final var notExisted = all.stream()
                .filter(toSave -> !existed.containsKey(toSave.getCurrency()))
                .map(toSave -> mapper.map(toSave))
                .map(repository::save)
                .map(mapper::map)
                .collect(Collectors.toMap(
                        ExchangeRate::getCurrency,
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
                .map(rateEntity -> updateIfCurrentProviderDoesNotExists(currenciesByName, rateEntity))
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
                .map(mapper::map)
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

    <T extends CurrencyRate> void saveHistory(final Collection<T> historicData) {
        final var currenciesByName = historicData.stream().collect(Collectors.groupingBy(CurrencyRate::getCurrency, toList()));
        final var savedHistory = historicData.stream()
                .map(mapper::map)
                .map(rateEntity -> updateIfCurrentProviderDoesNotExists(currenciesByName, rateEntity))
                .map(this::save)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(mapper::map)
                .collect(toList());
        log.info("{} historical currencies saved, {} duplicates skipped.", savedHistory.size(), historicData.size() - savedHistory.size());
    }

    private Optional<ExchangeRateEntity> save(final ExchangeRate exchangeRate) {
        try {
            return Optional.of(repository.save(mapper.map(exchangeRate)));
        } catch (final DataIntegrityViolationException exception) {
            log.debug("Currency {} for date {} already exists and won't be saved.", exchangeRate.getCurrency(), exchangeRate.getDate());
            return Optional.empty();
        }
    }

    private <T extends CurrencyRate> ExchangeRate updateIfCurrentProviderDoesNotExists(final Map<CurrencyCode, List<T>> currenciesByName, final ExchangeRateEntity rateEntity) {
        final var exchangeRate = mapper.map(rateEntity);
        if (!exchangeRate.hasProvider(provider.getName())) {
            log.info("Currency {} for date {} already exists but without current provider. Will be updated.", exchangeRate.getCurrency(), exchangeRate.getDate());
            final var toUpdateOpt = currenciesByName.getOrDefault(exchangeRate.getCurrency(), Collections.emptyList())
                    .stream()
                    .filter(currencyRate -> currencyRate.getDate().equals(exchangeRate.getDate()))
                    .findFirst();
            if (toUpdateOpt.isPresent()) {
                final var toUpdate = toUpdateOpt.get();
                exchangeRate.addRateForProvider(provider.getName(), toUpdate.getRate());
                exchangeRate.addDetailsForProvider(provider.getName(), toUpdate.getDetails());
            }
            final var updated = repository.save(mapper.map(exchangeRate));
            return mapper.map(updated);
        }
        return mapper.map(rateEntity);
    }

    private <T extends CurrencyRate> LocalDate findDateTo(final Collection<T> currencyRates) {
        return currencyRates.stream().max(Comparator.comparing(CurrencyRate::getDate)).map(CurrencyRate::getDate).orElseThrow(() -> new IllegalArgumentException("Invalid data, could not find date from"));
    }

    private <T extends CurrencyRate> LocalDate findDateFrom(final Collection<T> currencyRates) {
        return currencyRates.stream().min(Comparator.comparing(CurrencyRate::getDate)).map(CurrencyRate::getDate).orElseThrow(() -> new IllegalArgumentException("Invalid data, could not find date to"));
    }

    private Collection<ExchangeRateEntity> findByDateOrDateRange(final CurrencyCode currency, final LocalDate fromDate, final LocalDate toDate) {
        if (!fromDate.isEqual(toDate)) {
            return repository.findByCurrencyAndDateBetween(currency, mapper.fromLocalDateToInstant(fromDate).minus(1, ChronoUnit.DAYS), mapper.fromLocalDateToInstant(toDate).plus(1, ChronoUnit.DAYS));
        }
        return repository.findByCurrencyAndDate(currency, mapper.fromLocalDateToInstant(fromDate)).map(List::of).orElse(Collections.emptyList());
    }

    private Collection<ExchangeRateEntity> findByDateOrDateRange(final Set<CurrencyCode> currencies, final LocalDate fromDate, final LocalDate toDate) {
        if (!fromDate.isEqual(toDate)) {
            return repository.findByCurrencyInAndDateBetween(currencies, mapper.fromLocalDateToInstant(fromDate).minus(1, ChronoUnit.DAYS), mapper.fromLocalDateToInstant(toDate).plus(1, ChronoUnit.DAYS));
        }
        return repository.findByCurrencyInAndDate(currencies, mapper.fromLocalDateToInstant(fromDate));
    }

    private <T extends CurrencyRate> Map<CurrencyCode, Map<LocalDate, List<ExchangeRateEntity>>> mapMissingDates(final Map<CurrencyCode, Map<LocalDate, List<ExchangeRate>>> existed, final CurrencyCode currencyCode, final Map<LocalDate, List<T>> groupedByDate) {
        final var existedGroupedByDate = existed.get(currencyCode);
        return groupedByDate.entrySet()
                .stream()
                .filter(currencyRate -> !existedGroupedByDate.containsKey(currencyRate.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .map(toSave -> mapper.map(toSave))
                .collect(Collectors.groupingBy(ExchangeRateEntity::getCurrency, Collectors.groupingBy(entity -> mapper.fromInstantToLocalDate(entity.getDate()))));
    }

    private <T extends CurrencyRate> Map<CurrencyCode, Map<LocalDate, List<ExchangeRateEntity>>> mapMissingCurrencies(final Map<CurrencyCode, Map<LocalDate, List<ExchangeRate>>> existed, final Map.Entry<CurrencyCode, Map<LocalDate, List<T>>> currencyRate) {
        final var currencyCode = currencyRate.getKey();
        final var groupedByDate = currencyRate.getValue();

        return existed.getOrDefault(currencyCode, Collections.emptyMap()).isEmpty()
                ? groupedByDate.values()
                .stream()
                .flatMap(Collection::stream)
                .map(toSave -> mapper.map(toSave))
                .collect(Collectors.groupingBy(ExchangeRateEntity::getCurrency, Collectors.groupingBy(entity -> mapper.fromInstantToLocalDate(entity.getDate()))))
                : mapMissingDates(existed, currencyCode, groupedByDate);
    }

    private ExchangeRate getTheNewest(final ExchangeRate first, final ExchangeRate second) {
        final var firstDate = first.getDate();
        final var secondDate = second.getDate();

        return firstDate.isAfter(secondDate) ? first : second;
    }

}
