package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRate;
import mm.expenses.manager.finance.financial.CurrencyRate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
class ExchangeRateCreator {

    private final ExchangeRateRepository repository;
    private final ExchangeRateMapper mapper;

    <T extends CurrencyRate> Optional<ExchangeRate> create(final T currencyRate) {
        final var exist = repository.findByCurrencyAndDate(currencyRate.getCurrency(), mapper.fromLocalDateToInstant(currencyRate.getDate()));
        if (exist.isPresent()) {
            log.info("Currency {} for date {} already exists", currencyRate.getCurrency(), currencyRate.getDate());
            return exist.map(mapper::map);
        }
        final var saved = repository.save(mapper.map(currencyRate));
        return Optional.ofNullable(mapper.map(saved));
    }

    <T extends CurrencyRate> Collection<ExchangeRate> createForDateRange(final CurrencyCode currency, final Collection<T> forDateRange) {
        final var fromDate = findDateFrom(forDateRange);
        final var toDate = findDateTo(forDateRange);

        final var existed = findByDateOrDateRange(currency, fromDate, toDate).stream()
                .map(mapper::map)
                .collect(Collectors.toMap(
                        ExchangeRate::getDate,
                        Function.identity()
                ));

        final var notExisted = forDateRange.stream()
                .filter(toSave -> !existed.containsKey(toSave.getDate()))
                .map(mapper::map)
                .map(repository::save)
                .map(mapper::map)
                .collect(Collectors.toMap(
                        ExchangeRate::getDate,
                        Function.identity()
                ));

        log.info("{} currencies exist and won't be saved, {} new saved", existed.size(), notExisted.size());
        return Stream.concat(existed.values().stream(), notExisted.values().stream()).collect(Collectors.toList());
    }

    <T extends CurrencyRate> Collection<ExchangeRate> createAll(final Collection<T> all) {
        final var currencies = all.stream().map(CurrencyRate::getCurrency).collect(Collectors.toSet());
        final var fromDate = findDateFrom(all);
        final var toDate = findDateTo(all);

        final var existed = findByDateOrDateRange(currencies, fromDate, toDate).stream()
                .map(mapper::map)
                .collect(Collectors.toMap(
                        ExchangeRate::getCurrency,
                        Function.identity(),
                        this::getTheNewest
                ));

        final var notExisted = all.stream()
                .filter(toSave -> !existed.containsKey(toSave.getCurrency()))
                .map(mapper::map)
                .map(repository::save)
                .map(mapper::map)
                .collect(Collectors.toMap(
                        ExchangeRate::getCurrency,
                        Function.identity()
                ));

        log.info("{} currencies exist and won't be saved, {} new saved", existed.size(), notExisted.size());
        return Stream.concat(existed.values().stream(), notExisted.values().stream()).collect(Collectors.toList());
    }

    <T extends CurrencyRate> Collection<ExchangeRate> createAllForDateRange(final Collection<T> allForDateRange) {
        final var currencies = allForDateRange.stream().map(CurrencyRate::getCurrency).collect(Collectors.toSet());
        final var fromDate = findDateFrom(allForDateRange);
        final var toDate = findDateTo(allForDateRange);

        final var existedByCurrency = findByDateOrDateRange(currencies, fromDate, toDate).stream()
                .map(mapper::map)
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
                        .collect(Collectors.toList())
                )
                .flatMap(Collection::stream)
                .map(repository::save)
                .map(mapper::map)
                .collect(Collectors.toList());

        final var existed = existedByCurrency.values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        log.info("{} currencies exist and won't be saved, {} new saved", existed.size(), notExisted.size());
        return Stream.concat(existed.stream(), notExisted.stream()).collect(Collectors.toList());
    }

    <T extends CurrencyRate> void saveHistory(final Collection<T> historicData) {
        final var savedHistory = historicData.stream()
                .map(mapper::map)
                .map(this::save)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(mapper::map)
                .collect(Collectors.toList());
        log.info("{} historical currencies saved, {} duplicates skipped.", savedHistory.size(), historicData.size());
    }

    private Optional<ExchangeRateEntity> save(final ExchangeRateEntity entity) {
        try {
            return Optional.of(repository.save(entity));
        } catch(final DataIntegrityViolationException exception) {
            log.debug("Currency {} for date {} already exists and won't be saved.", entity.getCurrency(), entity.getDate());
            return Optional.empty();
        }
    }

    private <T extends CurrencyRate> Map<CurrencyCode, Map<LocalDate, List<ExchangeRateEntity>>> mapMissingCurrencies(final Map<CurrencyCode, Map<LocalDate, List<ExchangeRate>>> existed, final Map.Entry<CurrencyCode, Map<LocalDate, List<T>>> currencyRate) {
        final var currencyCode = currencyRate.getKey();
        final var groupedByDate = currencyRate.getValue();

        return existed.getOrDefault(currencyCode, Collections.emptyMap()).isEmpty()
                ? groupedByDate.values()
                .stream()
                .flatMap(Collection::stream)
                .map(mapper::map)
                .collect(Collectors.groupingBy(ExchangeRateEntity::getCurrency, Collectors.groupingBy(entity -> mapper.fromInstantToLocalDate(entity.getDate()))))
                : mapMissingDates(existed, currencyCode, groupedByDate);
    }

    private <T extends CurrencyRate> Map<CurrencyCode, Map<LocalDate, List<ExchangeRateEntity>>> mapMissingDates(final Map<CurrencyCode, Map<LocalDate, List<ExchangeRate>>> existed, final CurrencyCode currencyCode, final Map<LocalDate, List<T>> groupedByDate) {
        final var existedGroupedByDate = existed.get(currencyCode);
        return groupedByDate.entrySet()
                .stream()
                .filter(currencyRate -> !existedGroupedByDate.containsKey(currencyRate.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .map(mapper::map)
                .collect(Collectors.groupingBy(ExchangeRateEntity::getCurrency, Collectors.groupingBy(entity -> mapper.fromInstantToLocalDate(entity.getDate()))));
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

    private <T extends CurrencyRate> LocalDate findDateTo(final Collection<T> currencyRates) {
        return currencyRates.stream().max(Comparator.comparing(CurrencyRate::getDate)).map(CurrencyRate::getDate).orElseThrow(() -> new IllegalArgumentException("Invalid data, could not find date from"));
    }

    private <T extends CurrencyRate> LocalDate findDateFrom(final Collection<T> currencyRates) {
        return currencyRates.stream().min(Comparator.comparing(CurrencyRate::getDate)).map(CurrencyRate::getDate).orElseThrow(() -> new IllegalArgumentException("Invalid data, could not find date to"));
    }

    private ExchangeRate getTheNewest(final ExchangeRate first, final ExchangeRate second) {
        final var firstDate = first.getDate();
        final var secondDate = second.getDate();

        return firstDate.isAfter(secondDate) ? first : second;
    }

}
