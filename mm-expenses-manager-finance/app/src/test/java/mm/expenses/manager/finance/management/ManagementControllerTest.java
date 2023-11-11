package mm.expenses.manager.finance.management;

import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.beans.exception.ExceptionMessage;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.trail.ExchangeRateTrailService;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static mm.expenses.manager.finance.exception.FinanceExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED;
import static mm.expenses.manager.finance.exchangerate.trail.ExchangeRateTrailHelper.createNewExchangeRateTrail;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ManagementControllerTest extends FinanceApplicationTest {

    private static final String BASE_URL = "/management";

    @MockBean
    private ExchangeRateService exchangeRateService;

    @MockBean
    private ExchangeRateTrailService exchangeRateTrailService;

    @Override
    protected void setupAfterEachTest() {
        reset(exchangeRateService);
        reset(exchangeRateTrailService);
    }


    @Nested
    class FetchAndSaveHistoricCurrencies {

        @Test
        void shouldCallExchangeRateHistoryUpdate() throws Exception {
            mockMvc.perform(post(historyUpdateUrl())).andExpect(status().isNoContent());
            verify(exchangeRateService).historyUpdate();
        }

    }

    @Nested
    class FindAllTrails {

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByOperationAndStateAndDate(final TrailOperation operation) throws Exception {
            // given
            final var affectedIds = List.of("1", "2");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 10;
            final var date = DateUtils.now();
            final var dateAsLocalDate = DateUtils.instantToLocalDateUTC(date);

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);
            final var pageRequest = PageRequest.of(0, 1);

            // when
            when(exchangeRateTrailService.findTrails(dateAsLocalDate, operation, operation.getState(), pageRequest)).thenReturn(new PageImpl<>(List.of(trail), pageRequest, 1));

            // then
            mockMvc.perform(get(trails() + "?operation=" + operation + "&state=" + operation.getState() + "&date=" + dateAsLocalDate + "&pageNumber=0" + "&pageSize=1"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.numberOfElements", is(1)))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))

                    .andExpect(jsonPath("$.content[0].operation", is(operation.toString())))
                    .andExpect(jsonPath("$.content[0].state", is(operation.getState().toString())))
                    .andExpect(jsonPath("$.content[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].evaluated", is(evaluatedCount)))
                    .andExpect(jsonPath("$.content[0].skipped", is(skippedCount)))
                    .andExpect(jsonPath("$.content[0].affectedIds", hasSize(affectedIds.size())))

                    .andExpect(jsonPath("$.content[0].affectedIds[0]", is(affectedIds.get(0))))
                    .andExpect(jsonPath("$.content[0].affectedIds[1]", is(affectedIds.get(1))));
        }

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByOperationAndState(final TrailOperation operation) throws Exception {
            // given
            final var affectedIds = List.of("3", "4");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 10;
            final var date = DateUtils.now();

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);
            final var pageRequest = PageRequest.of(0, 1);

            // when
            when(exchangeRateTrailService.findTrails(null, operation, operation.getState(), pageRequest)).thenReturn(new PageImpl<>(List.of(trail), pageRequest, 1));

            // then
            mockMvc.perform(get(trails() + "?operation=" + operation + "&state=" + operation.getState() + "&pageNumber=0" + "&pageSize=1"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.numberOfElements", is(1)))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))

                    .andExpect(jsonPath("$.content[0].operation", is(operation.toString())))
                    .andExpect(jsonPath("$.content[0].state", is(operation.getState().toString())))
                    .andExpect(jsonPath("$.content[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].evaluated", is(evaluatedCount)))
                    .andExpect(jsonPath("$.content[0].skipped", is(skippedCount)))
                    .andExpect(jsonPath("$.content[0].affectedIds", hasSize(affectedIds.size())))

                    .andExpect(jsonPath("$.content[0].affectedIds[0]", is(affectedIds.get(0))))
                    .andExpect(jsonPath("$.content[0].affectedIds[1]", is(affectedIds.get(1))));
        }

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByStateAndDate(final TrailOperation operation) throws Exception {
            // given
            final var affectedIds = List.of("5", "6");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 10;
            final var date = DateUtils.now();
            final var dateAsLocalDate = DateUtils.instantToLocalDateUTC(date);

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);
            final var pageRequest = PageRequest.of(0, 1);

            // when
            when(exchangeRateTrailService.findTrails(dateAsLocalDate, null, operation.getState(), pageRequest)).thenReturn(new PageImpl<>(List.of(trail), pageRequest, 1));

            // then
            mockMvc.perform(get(trails() + "?state=" + operation.getState() + "&date=" + dateAsLocalDate + "&pageNumber=0" + "&pageSize=1"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.numberOfElements", is(1)))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))

                    .andExpect(jsonPath("$.content[0].operation", is(operation.toString())))
                    .andExpect(jsonPath("$.content[0].state", is(operation.getState().toString())))
                    .andExpect(jsonPath("$.content[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].evaluated", is(evaluatedCount)))
                    .andExpect(jsonPath("$.content[0].skipped", is(skippedCount)))
                    .andExpect(jsonPath("$.content[0].affectedIds", hasSize(affectedIds.size())))

                    .andExpect(jsonPath("$.content[0].affectedIds[0]", is(affectedIds.get(0))))
                    .andExpect(jsonPath("$.content[0].affectedIds[1]", is(affectedIds.get(1))));
        }

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByOperationAndDate(final TrailOperation operation) throws Exception {
            // given
            final var affectedIds = List.of("4", "9");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 10;
            final var date = DateUtils.now();
            final var dateAsLocalDate = DateUtils.instantToLocalDateUTC(date);

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);
            final var pageRequest = PageRequest.of(0, 1);

            // when
            when(exchangeRateTrailService.findTrails(dateAsLocalDate, operation, null, pageRequest)).thenReturn(new PageImpl<>(List.of(trail), pageRequest, 1));

            // then
            mockMvc.perform(get(trails() + "?operation=" + operation + "&date=" + dateAsLocalDate + "&pageNumber=0" + "&pageSize=1"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.numberOfElements", is(1)))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))

                    .andExpect(jsonPath("$.content[0].operation", is(operation.toString())))
                    .andExpect(jsonPath("$.content[0].state", is(operation.getState().toString())))
                    .andExpect(jsonPath("$.content[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].evaluated", is(evaluatedCount)))
                    .andExpect(jsonPath("$.content[0].skipped", is(skippedCount)))
                    .andExpect(jsonPath("$.content[0].affectedIds", hasSize(affectedIds.size())))

                    .andExpect(jsonPath("$.content[0].affectedIds[0]", is(affectedIds.get(0))))
                    .andExpect(jsonPath("$.content[0].affectedIds[1]", is(affectedIds.get(1))));
        }

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByOperation(final TrailOperation operation) throws Exception {
            // given
            final var affectedIds = List.of("4", "8");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 10;
            final var date = DateUtils.now();

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);
            final var pageRequest = PageRequest.of(0, 1);

            // when
            when(exchangeRateTrailService.findTrails(null, operation, null, pageRequest)).thenReturn(new PageImpl<>(List.of(trail), pageRequest, 1));

            // then
            mockMvc.perform(get(trails() + "?operation=" + operation + "&pageNumber=0" + "&pageSize=1"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.numberOfElements", is(1)))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))

                    .andExpect(jsonPath("$.content[0].operation", is(operation.toString())))
                    .andExpect(jsonPath("$.content[0].state", is(operation.getState().toString())))
                    .andExpect(jsonPath("$.content[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].evaluated", is(evaluatedCount)))
                    .andExpect(jsonPath("$.content[0].skipped", is(skippedCount)))
                    .andExpect(jsonPath("$.content[0].affectedIds", hasSize(affectedIds.size())))

                    .andExpect(jsonPath("$.content[0].affectedIds[0]", is(affectedIds.get(0))))
                    .andExpect(jsonPath("$.content[0].affectedIds[1]", is(affectedIds.get(1))));
        }

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByState(final TrailOperation operation) throws Exception {
            // given
            final var affectedIds = List.of("7", "8");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 10;
            final var date = DateUtils.now();

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);
            final var pageRequest = PageRequest.of(0, 1);

            // when
            when(exchangeRateTrailService.findTrails(null, null, operation.getState(), pageRequest)).thenReturn(new PageImpl<>(List.of(trail), pageRequest, 1));

            // then
            mockMvc.perform(get(trails() + "?state=" + operation.getState() + "&pageNumber=0" + "&pageSize=1"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.numberOfElements", is(1)))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))

                    .andExpect(jsonPath("$.content[0].operation", is(operation.toString())))
                    .andExpect(jsonPath("$.content[0].state", is(operation.getState().toString())))
                    .andExpect(jsonPath("$.content[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].evaluated", is(evaluatedCount)))
                    .andExpect(jsonPath("$.content[0].skipped", is(skippedCount)))
                    .andExpect(jsonPath("$.content[0].affectedIds", hasSize(affectedIds.size())))

                    .andExpect(jsonPath("$.content[0].affectedIds[0]", is(affectedIds.get(0))))
                    .andExpect(jsonPath("$.content[0].affectedIds[1]", is(affectedIds.get(1))));
        }

        @ParameterizedTest
        @ArgumentsSource(TrailOperationArgument.class)
        void shouldFindByDate(final TrailOperation operation) throws Exception {
            // given
            final var affectedIds = List.of("9", "0");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 10;
            final var date = DateUtils.now();
            final var dateAsLocalDate = DateUtils.instantToLocalDateUTC(date);

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);
            final var pageRequest = PageRequest.of(0, 1);

            // when
            when(exchangeRateTrailService.findTrails(dateAsLocalDate, null, null, pageRequest)).thenReturn(new PageImpl<>(List.of(trail), pageRequest, 1));

            // then
            mockMvc.perform(get(trails() + "?date=" + dateAsLocalDate + "&pageNumber=0" + "&pageSize=1"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.numberOfElements", is(1)))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))

                    .andExpect(jsonPath("$.content[0].operation", is(operation.toString())))
                    .andExpect(jsonPath("$.content[0].state", is(operation.getState().toString())))
                    .andExpect(jsonPath("$.content[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].evaluated", is(evaluatedCount)))
                    .andExpect(jsonPath("$.content[0].skipped", is(skippedCount)))
                    .andExpect(jsonPath("$.content[0].affectedIds", hasSize(affectedIds.size())))

                    .andExpect(jsonPath("$.content[0].affectedIds[0]", is(affectedIds.get(0))))
                    .andExpect(jsonPath("$.content[0].affectedIds[1]", is(affectedIds.get(1))));
        }

        @Test
        void shouldFindAll() throws Exception {
            // given
            final var affectedIds = List.of("5", "8");
            final var evaluatedCount = affectedIds.size();
            final var skippedCount = 5;
            final var date = DateUtils.now();
            final var operation = TrailOperation.EXCHANGE_RATES_HISTORY_UPDATE.withStatus(TrailOperation.State.ERROR);

            final var trail = createNewExchangeRateTrail(operation, date, evaluatedCount, skippedCount, affectedIds);
            final var pageRequest = PageRequest.of(0, 1);

            // when
            when(exchangeRateTrailService.findTrails(null, null, null, pageRequest)).thenReturn(new PageImpl<>(List.of(trail), pageRequest, 1));

            // then
            mockMvc.perform(get(trails() + "?pageNumber=0" + "&pageSize=1"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.numberOfElements", is(1)))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))

                    .andExpect(jsonPath("$.content[0].operation", is(operation.toString())))
                    .andExpect(jsonPath("$.content[0].state", is(operation.getState().toString())))
                    .andExpect(jsonPath("$.content[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].evaluated", is(evaluatedCount)))
                    .andExpect(jsonPath("$.content[0].skipped", is(skippedCount)))
                    .andExpect(jsonPath("$.content[0].affectedIds", hasSize(affectedIds.size())))

                    .andExpect(jsonPath("$.content[0].affectedIds[0]", is(affectedIds.get(0))))
                    .andExpect(jsonPath("$.content[0].affectedIds[1]", is(affectedIds.get(1))));
        }

        @Test
        void shouldReturnBadRequest_whenPageSizeIsMissed() throws Exception {
            mockMvc.perform(get(trails() + "?pageNumber=" + 0))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getCode())))
                    .andExpect(jsonPath("$.message", is(PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenPageNumberIsMissed() throws Exception {
            mockMvc.perform(get(trails() + "?pageSize=" + 1))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getCode())))
                    .andExpect(jsonPath("$.message", is(PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

    }

    private String historyUpdateUrl() {
        return BASE_URL + "/exchange-rates/history-update";
    }

    private String trails() {
        return BASE_URL + "/exchange-rates/trails";
    }

}