package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.data.domain.*;
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

    Stream<Page<ExchangeRate>> findAllCurrenciesRates(final Set<CurrencyCode> currencies, final LocalDate date, final LocalDate from, final LocalDate to, final Pageable pageable) {
        final var page = pageRequest(pageable);
        return currencies.stream()
                .map(code -> {
                    if (Objects.nonNull(date)) {
                        return repository.findByCurrencyAndDate(code, instantOf(date)).map(List::of)
                                .map(result -> (Page<ExchangeRate>) new PageImpl<>(result, page, page.getPageSize()))
                                .orElse(Page.empty(page));
                    } else if (Objects.nonNull(from) && Objects.nonNull(to)) {
                        return repository.findByDateBetween(instantOf(from), instantOf(to), page);
                    } else {
                        return repository.findByCurrency(code, page);
                    }
                });
    }

    Stream<Page<ExchangeRate>> findAllForCurrencyRates(final CurrencyCode currency, final LocalDate from, final LocalDate to, final Pageable pageable) {
        final var page = pageRequest(pageable);
        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            return Stream.of(repository.findByCurrencyAndDateBetween(currency, instantOf(from), instantOf(to), page));
        } else {
            return Stream.of(repository.findByCurrency(currency, page));
        }
    }

    Page<ExchangeRate> findAllLatest(final Pageable pageable) {
        final var page = pageRequest(pageable);
        return repository.findByDate(mapper.fromLocalDateToInstant(LocalDate.now()), page);
    }

    Optional<ExchangeRate> findLatestForCurrency(final CurrencyCode currency) {
        return findByCurrencyAndDate(currency, LocalDate.now());
    }

    Optional<ExchangeRate> findByCurrencyAndDate(final CurrencyCode currency, final LocalDate date) {
        return repository.findByCurrencyAndDate(currency, instantOf(date));
    }

    PageRequest pageRequest(final Integer pageNumber, final Integer pageSize) {
        return PageRequest.of(pageNumber, pageSize, Sort.by("date").descending());
    }

    private PageRequest pageRequest(final Pageable pageable) {
        return pageRequest(pageable.getPageNumber(), pageable.getPageSize());
    }

    private Instant instantOf(final LocalDate date) {
        return mapper.fromLocalDateToInstant(date);
    }

}
