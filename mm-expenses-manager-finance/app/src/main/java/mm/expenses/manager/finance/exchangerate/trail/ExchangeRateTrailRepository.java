package mm.expenses.manager.finance.exchangerate.trail;

import mm.expenses.manager.finance.exchangerate.trail.TrailOperation.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
interface ExchangeRateTrailRepository extends MongoRepository<ExchangeRateTrail, String> {

    Page<ExchangeRateTrail> findByState(final State state, final Pageable pageable);

    Page<ExchangeRateTrail> findByDate(final LocalDate date, final Pageable pageable);

    Page<ExchangeRateTrail> findByOperation(final TrailOperation operation, final Pageable pageable);

    Page<ExchangeRateTrail> findByStateAndDate(final State state, final LocalDate date, final Pageable pageable);

    Page<ExchangeRateTrail> findByOperationAndState(final TrailOperation operation, final State state, final Pageable pageable);

    Page<ExchangeRateTrail> findByOperationAndDate(final TrailOperation operation, final LocalDate date, final Pageable pageable);

    Page<ExchangeRateTrail> findByOperationAndStateAndDate(final TrailOperation operation, final State state, final LocalDate date, final Pageable pageable);

}
