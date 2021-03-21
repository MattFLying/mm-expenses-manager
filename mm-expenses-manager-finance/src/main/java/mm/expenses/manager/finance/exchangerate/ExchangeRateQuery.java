package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
class ExchangeRateQuery {

    private final ExchangeRateRepository repository;
    private final ExchangeRateMapper mapper;

    Stream<ExchangeRate> findAllCurrenciesRates(final LocalDate date, final LocalDate from, final LocalDate to) {
        Stream<ExchangeRate> result;
        if (Objects.nonNull(date)) {
            result = repository.findByDate(instantOf(date));
        } else if (Objects.nonNull(from) && Objects.nonNull(to)) {
            result = repository.findByDateBetween(instantOf(from), instantOf(to));
        } else {
            result = repository.findAll().stream();
        }
        return result;
    }

    Stream<ExchangeRate> findAllForCurrencyRates(final CurrencyCode currency, final LocalDate from, final LocalDate to) {
        Stream<ExchangeRate> result;
        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            result = repository.findByCurrencyAndDateBetween(currency, instantOf(from), instantOf(to));
        } else {
            result = repository.findByCurrency(currency);
        }
        return result;
    }

    Stream<ExchangeRate> findAllLatest() {
        return repository.findByDate(mapper.fromLocalDateToInstant(LocalDate.now()));
    }

    Optional<ExchangeRate> findLatestForCurrency(final CurrencyCode currency) {
        return findByCurrencyAndDate(currency, LocalDate.now());
    }

    Optional<ExchangeRate> findByCurrencyAndDate(final CurrencyCode currency, final LocalDate date) {
        return repository.findByCurrencyAndDate(currency, instantOf(date));
    }

    private Instant instantOf(final LocalDate date) {
        return mapper.fromLocalDateToInstant(date);
    }

}
