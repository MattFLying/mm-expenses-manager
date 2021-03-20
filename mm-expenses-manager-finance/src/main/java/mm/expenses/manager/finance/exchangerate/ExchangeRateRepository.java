package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Repository
interface ExchangeRateRepository extends MongoRepository<ExchangeRateEntity, String> {

    Stream<ExchangeRateEntity> findByDate(final Instant date);

    Stream<ExchangeRateEntity> findByDateBetween(final Instant from, final Instant to);

    Stream<ExchangeRateEntity> findByCurrency(final CurrencyCode currency);

    Optional<ExchangeRateEntity> findByCurrencyAndDate(final CurrencyCode currency, final Instant date);

    Stream<ExchangeRateEntity> findByCurrencyInAndDate(final Set<CurrencyCode> currencies, final Instant date);

    Stream<ExchangeRateEntity> findByCurrencyAndDateBetween(final CurrencyCode currency, final Instant from, final Instant to);

    Stream<ExchangeRateEntity> findByCurrencyInAndDateBetween(final Set<CurrencyCode> currencies, final Instant from, final Instant to);

}
