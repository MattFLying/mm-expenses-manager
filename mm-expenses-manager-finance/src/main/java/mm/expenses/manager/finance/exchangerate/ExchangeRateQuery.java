package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.pageable.PageHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static mm.expenses.manager.common.util.DateUtils.localDateToInstantUTC;
import static mm.expenses.manager.finance.exchangerate.ExchangeRate.DEFAULT_SORT_ORDER;

@Component
@RequiredArgsConstructor
class ExchangeRateQuery {

    private final ExchangeRateRepository repository;

    Stream<Page<ExchangeRate>> findAllCurrenciesRates(final Collection<CurrencyCode> currencies, final LocalDate date, final LocalDate from, final LocalDate to, final Pageable pageable) {
        final var page = pageRequest(pageable);
        return currencies.stream()
                .map(code -> {
                    if (Objects.nonNull(date)) {
                        return repository.findByCurrencyAndDate(code, localDateToInstantUTC(date)).map(List::of)
                                .map(result -> (Page<ExchangeRate>) new PageImpl<>(result, page, page.getPageSize()))
                                .orElse(Page.empty(page));
                    } else if (Objects.nonNull(from) && Objects.nonNull(to)) {
                        return repository.findByCurrencyAndDateBetween(code, localDateToInstantUTC(from), localDateToInstantUTC(to), page);
                    } else {
                        return repository.findByCurrency(code, page);
                    }
                })
                .filter(Objects::nonNull);
    }

    Stream<Page<ExchangeRate>> findAllForCurrencyRates(final CurrencyCode currency, final LocalDate from, final LocalDate to, final Pageable pageable) {
        final var page = pageRequest(pageable);
        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            return Stream.of(repository.findByCurrencyAndDateBetween(currency, localDateToInstantUTC(from), localDateToInstantUTC(to), page)).filter(Objects::nonNull);
        } else {
            return Stream.of(repository.findByCurrency(currency, page)).filter(Objects::nonNull);
        }
    }

    Page<ExchangeRate> findAllTodayRates(final Pageable pageable) {
        final var page = pageRequest(pageable);
        return repository.findByDate(localDateToInstantUTC(LocalDate.now()), page);
    }

    Page<ExchangeRate> findByDate(final Pageable pageable, final Instant date) {
        final var page = pageRequest(pageable);
        return repository.findByDate(date, page);
    }

    Optional<ExchangeRate> findByCurrencyAndDate(final CurrencyCode currency, final LocalDate date) {
        return repository.findByCurrencyAndDate(currency, localDateToInstantUTC(date));
    }

    PageRequest pageRequest(final Integer pageNumber, final Integer pageSize) {
        return PageHelper.getPageRequest(pageNumber, pageSize, Sort.by(DEFAULT_SORT_ORDER));
    }

    PageRequest pageRequest(final Pageable pageable) {
        return pageRequest(pageable.getPageNumber(), pageable.getPageSize());
    }

}
