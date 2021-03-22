package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Repository
interface ExchangeRateRepository extends MongoRepository<ExchangeRate, String> {

    Optional<ExchangeRate> findByCurrencyAndDate(final CurrencyCode currency, final Instant date);

    Page<ExchangeRate> findByDate(final Instant date, final Pageable pageable);

    Page<ExchangeRate> findByDateBetween(final Instant from, final Instant to, final Pageable pageable);

    Page<ExchangeRate> findByCurrency(final CurrencyCode currency, final Pageable pageable);

    Page<ExchangeRate> findByCurrencyAndDateBetween(final CurrencyCode currency, final Instant from, final Instant to, final Pageable pageable);

    Stream<ExchangeRate> findByCurrencyInAndDate(final Set<CurrencyCode> currencies, final Instant date);

    Stream<ExchangeRate> findByCurrencyInAndDateBetween(final Set<CurrencyCode> currencies, final Instant from, final Instant to);

}
