package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
interface ExchangeRateRepository extends MongoRepository<ExchangeRateEntity, String> {

    Collection<ExchangeRateEntity> findByDate(final Instant date);

    Collection<ExchangeRateEntity> findByDateBetween(final Instant from, final Instant to);

    Collection<ExchangeRateEntity> findByCurrency(final CurrencyCode currency);

    Optional<ExchangeRateEntity> findByCurrencyAndDate(final CurrencyCode currency, final Instant date);

    Collection<ExchangeRateEntity> findByCurrencyInAndDate(final Set<CurrencyCode> currencies, final Instant date);

    Collection<ExchangeRateEntity> findByCurrencyAndDateBetween(final CurrencyCode currency, final Instant from, final Instant to);

    Collection<ExchangeRateEntity> findByCurrencyInAndDateBetween(final Set<CurrencyCode> currencies, final Instant from, final Instant to);

}
