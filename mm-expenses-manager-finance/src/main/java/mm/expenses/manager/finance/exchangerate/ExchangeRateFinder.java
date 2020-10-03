package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.common.CurrencyProviderType;
import mm.expenses.manager.finance.exchangerate.model.CurrencyRates;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class ExchangeRateFinder {

    private final ExchangeRateRepository repository;
    private final ExchangeRateMapper mapper;
    private final ExchangeRateConfig config;

    Collection<CurrencyRates> findAllCurrenciesRates(final LocalDate date, final LocalDate from, final LocalDate to) {
        final var provider = CurrencyProviderType.findByName(config.getProvider());

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
                .map(entry -> mapper.map(entry.getKey(), entry.getValue(), provider))
                .sorted(Comparator.comparing(CurrencyRates::getCurrency))
                .collect(Collectors.toList());
    }

    Collection<CurrencyRates> findAllForCurrencyRates(final CurrencyCode currency, final LocalDate date, final LocalDate from, final LocalDate to) {
        final var provider = CurrencyProviderType.findByName(config.getProvider());

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
                .map(entry -> mapper.map(entry.getKey(), entry.getValue(), provider))
                .sorted(Comparator.comparing(CurrencyRates::getCurrency))
                .collect(Collectors.toList());
    }

}
