package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRates;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class ExchangeRateQuery {

    private final ExchangeRateRepository repository;
    private final ExchangeRateMapper mapper;

    Collection<ExchangeRates> findAllCurrenciesRates(final LocalDate date, final LocalDate from, final LocalDate to) {
        Collection<ExchangeRateEntity> result;
        if (Objects.nonNull(date)) {
            result = repository.findByDate(mapper.fromLocalDateToInstant(date));
        } else if (Objects.nonNull(from) && Objects.nonNull(to)) {
            result = repository.findByDateBetween(mapper.fromLocalDateToInstant(from), mapper.fromLocalDateToInstant(to));
        } else {
            result = repository.findAll();
        }
        return result.stream()
                .collect(Collectors.groupingBy(ExchangeRateEntity::getCurrency))
                .entrySet()
                .stream()
                .map(entry -> mapper.map(entry.getKey(), sortByDateByTheNewest(entry.getValue())))
                .sorted(Comparator.comparing(ExchangeRates::getCurrency))
                .collect(Collectors.toList());
    }

    Collection<ExchangeRates> findAllForCurrencyRates(final CurrencyCode currency, final LocalDate date, final LocalDate from, final LocalDate to) {
        Collection<ExchangeRateEntity> result;
        if (Objects.nonNull(date)) {
            result = repository.findByCurrencyAndDate(currency, mapper.fromLocalDateToInstant(date))
                    .<Collection<ExchangeRateEntity>>map(List::of)
                    .orElseGet(List::of);
        } else if (Objects.nonNull(from) && Objects.nonNull(to)) {
            result = repository.findByCurrencyAndDateBetween(currency, mapper.fromLocalDateToInstant(from), mapper.fromLocalDateToInstant(to));
        } else {
            result = repository.findByCurrency(currency);
        }
        return result.stream()
                .collect(Collectors.groupingBy(ExchangeRateEntity::getCurrency))
                .entrySet()
                .stream()
                .map(entry -> mapper.map(entry.getKey(), sortByDateByTheNewest(entry.getValue())))
                .sorted(Comparator.comparing(ExchangeRates::getCurrency))
                .collect(Collectors.toList());
    }

    private Collection<ExchangeRateEntity> sortByDateByTheNewest(final Collection<ExchangeRateEntity> entities) {
        return entities.stream().sorted(Comparator.comparing(ExchangeRateEntity::getDate).reversed()).collect(Collectors.toList());
    }

}
