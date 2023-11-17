package mm.expenses.manager.finance.exchangerate;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.pagination.PaginationHelper;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static mm.expenses.manager.common.utils.util.DateUtils.localDateToInstant;

@Component
@RequiredArgsConstructor
class ExchangeRateQuery {

    private final ExchangeRateRepository repository;
    private final PaginationHelper pagination;

    Stream<Page<ExchangeRate>> findAllCurrenciesRates(final Collection<CurrencyCode> currencies, final LocalDate date, final LocalDate from, final LocalDate to, final Pageable pageable) {
        final var page = pageRequest(pageable);
        return currencies.stream()
                .map(code -> {
                    if (Objects.nonNull(date)) {
                        return repository.findByCurrencyAndDate(code, localDateToInstant(date)).map(List::of)
                                .map(result -> (Page<ExchangeRate>) new PageImpl<>(result, page, page.getPageSize()))
                                .orElse(Page.empty(page));
                    } else if (Objects.nonNull(from) && Objects.nonNull(to)) {
                        return repository.findByCurrencyAndDateBetween(code, localDateToInstant(from), localDateToInstant(to), page);
                    } else {
                        return repository.findByCurrency(code, page);
                    }
                })
                .filter(Objects::nonNull);
    }

    Stream<Page<ExchangeRate>> findAllForCurrencyRates(final CurrencyCode currency, final LocalDate from, final LocalDate to, final Pageable pageable) {
        final var page = pageRequest(pageable);
        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            return Stream.of(repository.findByCurrencyAndDateBetween(currency, localDateToInstant(from), localDateToInstant(to), page)).filter(Objects::nonNull);
        } else {
            return Stream.of(repository.findByCurrency(currency, page)).filter(Objects::nonNull);
        }
    }

    Page<ExchangeRate> findAllTodayRates(final Pageable pageable) {
        final var page = pageRequest(pageable);
        return repository.findByDate(localDateToInstant(LocalDate.now()), page);
    }

    Page<ExchangeRate> findByDate(final Pageable pageable, final Instant date) {
        final var page = pageRequest(pageable);
        return repository.findByDate(date, page);
    }

    Optional<ExchangeRate> findByCurrencyAndDate(final CurrencyCode currency, final LocalDate date) {
        return repository.findByCurrencyAndDate(currency, localDateToInstant(date));
    }

    PageRequest pageRequest(final Integer pageNumber, final Integer pageSize) {
        return pagination.getPageRequest(pageNumber, pageSize, ExchangeRateSortOrder.DEFAULT_SORT);
    }

    PageRequest pageRequest(final Pageable pageable) {
        return pageRequest(pageable.getPageNumber(), pageable.getPageSize());
    }

}
