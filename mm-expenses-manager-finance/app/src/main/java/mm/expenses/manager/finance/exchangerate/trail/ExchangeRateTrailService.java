package mm.expenses.manager.finance.exchangerate.trail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.StringJoiner;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateTrailService {

    private final ExchangeRateTrailRepository repository;

    /**
     * Save details of executed operation on exchange rates.
     *
     * @param exchangeRatesOperation operation type
     * @param affectedIds            ids of exchange rates that were affected and should be saved
     * @param evaluatedCount         how many exchange rates were evaluated
     * @param skippedCount           how many exchange rates were skipped
     */
    public void saveLog(final TrailOperation exchangeRatesOperation, final Collection<String> affectedIds, final long evaluatedCount, final long skippedCount) {
        final var state = exchangeRatesOperation.getState();
        final var trail = ExchangeRateTrail.builder()
                .operation(exchangeRatesOperation)
                .state(state)
                .date(DateUtils.now())
                .evaluated(evaluatedCount)
                .skipped(skippedCount)
                .affectedIds(affectedIds)
                .build();

        repository.save(trail);
        log.info("Executed {} with status {}, {} exchange rates has been evaluated, {} exchange rates has been skipped.", exchangeRatesOperation, state, evaluatedCount, skippedCount);
    }

    /**
     * Finds trails regarding to passed parameters.
     *
     * @param date      date of executed oepration
     * @param operation operation type
     * @param state     status of operation
     * @param pageable  pageable details
     * @return page with trails
     */
    public Page<ExchangeRateTrail> findTrails(final LocalDate date, final TrailOperation operation, final State state, final Pageable pageable) {
        final var type = findType(operation, state, date);
        switch (type) {
            case OPERATION:
                return repository.findByOperation(operation, pageable);
            case STATE:
                return repository.findByState(state, pageable);
            case DATE:
                return repository.findByDate(date, pageable);
            case STATE_DATE:
                return repository.findByStateAndDate(state, date, pageable);
            case OPERATION_DATE:
                return repository.findByOperationAndDate(operation, date, pageable);
            case OPERATION_STATE:
                return repository.findByOperationAndState(operation, state, pageable);
            case OPERATION_STATE_DATE:
                return repository.findByOperationAndStateAndDate(operation, state, date, pageable);
            default:
                return repository.findAll(pageable);
        }
    }

    private FindType findType(final TrailOperation operation, final State state, final LocalDate date) {
        final var joiner = new StringJoiner("_");
        try {
            if (Objects.nonNull(operation)) {
                joiner.add("operation");
            }
            if (Objects.nonNull(state)) {
                joiner.add("state");
            }
            if (Objects.nonNull(date)) {
                joiner.add("date");
            }
            return FindType.valueOf(joiner.toString().toUpperCase());
        } catch (final IllegalArgumentException exception) {
            log.debug("Invalid operation has been created: {}", joiner, exception);
            return FindType.ALL;
        }
    }

    private enum FindType {
        ALL, OPERATION, STATE, DATE, OPERATION_DATE, STATE_DATE, OPERATION_STATE, OPERATION_STATE_DATE
    }

}
