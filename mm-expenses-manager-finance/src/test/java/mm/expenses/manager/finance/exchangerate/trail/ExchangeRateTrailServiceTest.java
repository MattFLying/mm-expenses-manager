package mm.expenses.manager.finance.exchangerate.trail;

import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation.State;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static mm.expenses.manager.finance.exchangerate.trail.ExchangeRateTrailAssert.assertExchangeRateTrail;
import static mm.expenses.manager.finance.exchangerate.trail.ExchangeRateTrailHelper.createNewExchangeRateTrail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExchangeRateTrailServiceTest extends FinanceApplicationTest {

    @MockBean
    private ExchangeRateTrailRepository exchangeRateTrailRepository;

    @Autowired
    private ExchangeRateTrailService exchangeRateTrailService;

    @Override
    protected void setupAfterEachTest() {
        reset(exchangeRateTrailRepository);
    }


    @Nested
    class SaveLog {

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldSaveTrailDetails(final TrailOperation operation) {
            // given
            final var affectedIds = List.of("1", "2");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 10L;

            // when
            exchangeRateTrailService.saveLog(operation, affectedIds, evaluatedCount, skippedCount);

            // then
            verify(exchangeRateTrailRepository).save(any(ExchangeRateTrail.class));
        }

    }

    @Nested
    class FindTrails {

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByOperationAndStateAndDate(final TrailOperation operation) {
            // given
            final var affectedIds = List.of("3", "4", "5");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 10L;
            final var date = DateUtils.now();
            final var dateAsLocalDate = DateUtils.instantToLocalDateUTC(date);

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);

            // when
            when(exchangeRateTrailRepository.findByOperationAndStateAndDate(any(TrailOperation.class), any(State.class), any(LocalDate.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(trail)));
            final var resultPage = exchangeRateTrailService.findTrails(dateAsLocalDate, operation, operation.getState(), PageRequest.of(0, 1));

            // then
            assertThat(resultPage).isNotNull().hasSize(1);
            final var result = resultPage.getContent().get(0);
            assertExchangeRateTrail(result)
                    .hasId(trail.getId())
                    .isOfOperation(trail.getOperation())
                    .hasState(trail.getState())
                    .hasEvaluated(trail.getEvaluated())
                    .hasSkipped(trail.getSkipped())
                    .ofDate(trail.getDate())
                    .hasAffectedIds(trail.getAffectedIds());
        }

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByOperation(final TrailOperation operation) {
            // given
            final var affectedIds = List.of("6");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 5L;
            final var date = DateUtils.now();

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);

            // when
            when(exchangeRateTrailRepository.findByOperation(any(TrailOperation.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(trail)));
            final var resultPage = exchangeRateTrailService.findTrails(null, operation, null, PageRequest.of(0, 1));

            // then
            assertThat(resultPage).isNotNull().hasSize(1);
            final var result = resultPage.getContent().get(0);
            assertExchangeRateTrail(result)
                    .hasId(trail.getId())
                    .isOfOperation(trail.getOperation())
                    .hasState(trail.getState())
                    .hasEvaluated(trail.getEvaluated())
                    .hasSkipped(trail.getSkipped())
                    .ofDate(trail.getDate())
                    .hasAffectedIds(trail.getAffectedIds());
        }

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByState(final TrailOperation operation) {
            // given
            final var evaluatedCount = 0L;
            final var skippedCount = 5L;
            final var date = DateUtils.now();

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, Collections.emptyList());

            // when
            when(exchangeRateTrailRepository.findByState(any(State.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(trail)));
            final var resultPage = exchangeRateTrailService.findTrails(null, null, operation.getState(), PageRequest.of(0, 1));

            // then
            assertThat(resultPage).isNotNull().hasSize(1);
            final var result = resultPage.getContent().get(0);
            assertExchangeRateTrail(result)
                    .hasId(trail.getId())
                    .isOfOperation(trail.getOperation())
                    .hasState(trail.getState())
                    .hasEvaluated(trail.getEvaluated())
                    .hasSkipped(trail.getSkipped())
                    .ofDate(trail.getDate())
                    .hasAffectedIds(trail.getAffectedIds());
        }

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByDate(final TrailOperation operation) {
            // given
            final var evaluatedCount = 0L;
            final var skippedCount = 3L;
            final var date = DateUtils.now();
            final var dateAsLocalDate = DateUtils.instantToLocalDateUTC(date);

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, Collections.emptyList());

            // when
            when(exchangeRateTrailRepository.findByDate(any(LocalDate.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(trail)));
            final var resultPage = exchangeRateTrailService.findTrails(dateAsLocalDate, null, null, PageRequest.of(0, 1));

            // then
            assertThat(resultPage).isNotNull().hasSize(1);
            final var result = resultPage.getContent().get(0);
            assertExchangeRateTrail(result)
                    .hasId(trail.getId())
                    .isOfOperation(trail.getOperation())
                    .hasState(trail.getState())
                    .hasEvaluated(trail.getEvaluated())
                    .hasSkipped(trail.getSkipped())
                    .ofDate(trail.getDate())
                    .hasAffectedIds(trail.getAffectedIds());
        }

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByOperationAndState(final TrailOperation operation) {
            // given
            final var affectedIds = List.of("6", "7");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 3L;
            final var date = DateUtils.now();

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);

            // when
            when(exchangeRateTrailRepository.findByOperationAndState(any(TrailOperation.class), any(State.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(trail)));
            final var resultPage = exchangeRateTrailService.findTrails(null, operation, operation.getState(), PageRequest.of(0, 1));

            // then
            assertThat(resultPage).isNotNull().hasSize(1);
            final var result = resultPage.getContent().get(0);
            assertExchangeRateTrail(result)
                    .hasId(trail.getId())
                    .isOfOperation(trail.getOperation())
                    .hasState(trail.getState())
                    .hasEvaluated(trail.getEvaluated())
                    .hasSkipped(trail.getSkipped())
                    .ofDate(trail.getDate())
                    .hasAffectedIds(trail.getAffectedIds());
        }

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByStateAndDate(final TrailOperation operation) {
            // given
            final var affectedIds = List.of("6", "7");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 3L;
            final var date = DateUtils.now();
            final var dateAsLocalDate = DateUtils.instantToLocalDateUTC(date);

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);

            // when
            when(exchangeRateTrailRepository.findByStateAndDate(any(State.class), any(LocalDate.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(trail)));
            final var resultPage = exchangeRateTrailService.findTrails(dateAsLocalDate, null, operation.getState(), PageRequest.of(0, 1));

            // then
            assertThat(resultPage).isNotNull().hasSize(1);
            final var result = resultPage.getContent().get(0);
            assertExchangeRateTrail(result)
                    .hasId(trail.getId())
                    .isOfOperation(trail.getOperation())
                    .hasState(trail.getState())
                    .hasEvaluated(trail.getEvaluated())
                    .hasSkipped(trail.getSkipped())
                    .ofDate(trail.getDate())
                    .hasAffectedIds(trail.getAffectedIds());
        }

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByOperationAndDate(final TrailOperation operation) {
            // given
            final var affectedIds = List.of("8", "9");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 0L;
            final var date = DateUtils.now();
            final var dateAsLocalDate = DateUtils.instantToLocalDateUTC(date);

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);

            // when
            when(exchangeRateTrailRepository.findByOperationAndDate(any(TrailOperation.class), any(LocalDate.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(trail)));
            final var resultPage = exchangeRateTrailService.findTrails(dateAsLocalDate, operation, null, PageRequest.of(0, 1));

            // then
            assertThat(resultPage).isNotNull().hasSize(1);
            final var result = resultPage.getContent().get(0);
            assertExchangeRateTrail(result)
                    .hasId(trail.getId())
                    .isOfOperation(trail.getOperation())
                    .hasState(trail.getState())
                    .hasEvaluated(trail.getEvaluated())
                    .hasSkipped(trail.getSkipped())
                    .ofDate(trail.getDate())
                    .hasAffectedIds(trail.getAffectedIds());
        }

        @Test
        void shouldFindAll() {
            // given
            final var affectedIds = List.of("3", "4", "5", "6");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 10L;
            final var date = DateUtils.now();
            final var operation = TrailOperation.LATEST_EXCHANGE_RATES_SYNCHRONIZATION.withStatus(State.SUCCESS);

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);

            // when
            final var pageRequest = PageRequest.of(0, 1);
            when(exchangeRateTrailRepository.findAll(pageRequest)).thenReturn(new PageImpl<>(List.of(trail)));
            final var resultPage = exchangeRateTrailService.findTrails(null, null, null, pageRequest);

            // then
            assertThat(resultPage).isNotNull().hasSize(1);
            final var result = resultPage.getContent().get(0);
            assertExchangeRateTrail(result)
                    .hasId(trail.getId())
                    .isOfOperation(trail.getOperation())
                    .hasState(trail.getState())
                    .hasEvaluated(trail.getEvaluated())
                    .hasSkipped(trail.getSkipped())
                    .ofDate(trail.getDate())
                    .hasAffectedIds(trail.getAffectedIds());
        }

    }

}