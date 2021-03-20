package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Repository
interface ExchangeRateRepository extends MongoRepository<ExchangeRate, String> {

    Stream<ExchangeRate> findByDate(final Instant date);

    Stream<ExchangeRate> findByDateBetween(final Instant from, final Instant to);

    Stream<ExchangeRate> findByCurrency(final CurrencyCode currency);

    Optional<ExchangeRate> findByCurrencyAndDate(final CurrencyCode currency, final Instant date);

    Stream<ExchangeRate> findByCurrencyInAndDate(final Set<CurrencyCode> currencies, final Instant date);

    Stream<ExchangeRate> findByCurrencyAndDateBetween(final CurrencyCode currency, final Instant from, final Instant to);

    Stream<ExchangeRate> findByCurrencyInAndDateBetween(final Set<CurrencyCode> currencies, final Instant from, final Instant to);

}
