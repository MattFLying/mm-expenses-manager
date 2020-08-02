package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

@Repository
interface ExchangeRateRepository extends MongoRepository<ExchangeRateEntity, String> {

    Optional<ExchangeRateEntity> findByCurrencyAndDate(final CurrencyCode currency, final String date);

    Collection<ExchangeRateEntity> findByCurrencyAndDateBetween(final CurrencyCode currency, final Instant from, final Instant to);

}
