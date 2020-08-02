package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRate;
import mm.expenses.manager.finance.financial.CurrencyRate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
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
        final var exist = repository.findByCurrencyAndDate(currencyRate.getCurrency(), currencyRate.getDate().toString());
        if (exist.isPresent()) {
            log.info("Currency {} for date {} already exists", currencyRate.getCurrency(), currencyRate.getDate());
            return exist.map(mapper::map);
        }
        final var saved = repository.save(mapper.map(currencyRate));
        return Optional.ofNullable(mapper.map(saved));
    }

    <T extends CurrencyRate> Collection<ExchangeRate> createForDateRange(final Collection<T> forDateRange) {
        final var existed = forDateRange.stream()
                .map(rate -> repository.findByCurrencyAndDate(rate.getCurrency(), rate.getDate().toString()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(mapper::map)
                .collect(Collectors.toMap(
                        ExchangeRate::getDate,
                        Function.identity()
                ));

        final var savedFiltered = forDateRange.stream()
                .filter(rate -> !existed.containsKey(rate.getDate()))
                .map(mapper::map)
                .map(repository::save)
                .map(mapper::map);

        if (existed.isEmpty()) {
            return savedFiltered.collect(Collectors.toList());
        } else {
            return Stream.concat(savedFiltered, existed.values().stream()).collect(Collectors.toList());
        }
    }

    <T extends CurrencyRate> Collection<ExchangeRate> createAll(final Collection<T> all) {
        final var groupedByCurrency = all.stream().collect(Collectors.groupingBy(CurrencyRate::getCurrency));

        return groupedByCurrency.values()
                .stream()
                .map(this::createForDateRange)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}
