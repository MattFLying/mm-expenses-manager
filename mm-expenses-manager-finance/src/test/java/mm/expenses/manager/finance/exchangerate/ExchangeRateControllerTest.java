package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.FinanceApplicationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static mm.expenses.manager.finance.exchangerate.ExchangeRateHelper.createNewExchangeRate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExchangeRateControllerTest extends FinanceApplicationTest {

    private static final String BASE_URL = "/exchange-rates";

    @MockBean
    private ExchangeRateRepository repository;

    @Override
    protected void setupAfterEachTest() {
        reset(repository);
    }

    @Nested
    class Get {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldRetrieveAllExchangeRates_whenNoParamsPassed(final CurrencyCode currency) throws Exception {
            // given
            final var expectedContentSize = 2;
            final var expectedPagesSize = 1;

            final var date = LocalDate.now().minusDays(5);
            final var dateAsInstant = DateUtils.localDateToInstant(date);

            final var expected_1 = createNewExchangeRate(currency, dateAsInstant);
            final var rate_1 = expected_1.getRateByProvider(PROVIDER_NAME);

            final var expected_2 = createNewExchangeRate(currency, dateAsInstant);
            final var rate_2 = expected_2.getRateByProvider(PROVIDER_NAME);

            // when
            when(repository.findByCurrency(eq(currency), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1, expected_2)));

            // then
            mockMvc.perform(get(BASE_URL))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currencies", is(expectedPagesSize)))
                    .andExpect(jsonPath("$.exchangeRates", is(expectedContentSize)))
                    .andExpect(jsonPath("$.totalExchangeRates", is(expectedContentSize)))

                    .andExpect(jsonPath("$.content[0].numberOfElements", is(expectedContentSize)))
                    .andExpect(jsonPath("$.content[0].totalElements", is(expectedContentSize)))
                    .andExpect(jsonPath("$.content[0].totalPages", is(expectedPagesSize)))
                    .andExpect(jsonPath("$.content[0].content.currency", is(currency.toString())))
                    .andExpect(jsonPath("$.content[0].content.rates", hasSize(expectedContentSize)))

                    .andExpect(jsonPath("$.content[0].content.rates[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.from.currency", is(rate_1.getFrom().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.from.value", is(rate_1.getFrom().getValue())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.to.currency", is(rate_1.getTo().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.to.value", is(rate_1.getTo().getValue())))

                    .andExpect(jsonPath("$.content[0].content.rates[1].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.from.currency", is(rate_2.getFrom().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.from.value", is(rate_2.getFrom().getValue())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.to.currency", is(rate_2.getTo().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.to.value", is(rate_2.getTo().getValue())));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldRetrievePagedExchangeRates_whenPageParamsPassed(final CurrencyCode currency) throws Exception {
            // given
            final var expectedContentPerPage = 1;
            final var expectedTotalContentSize = 2;

            final var date = LocalDate.now().minusDays(5);
            final var dateAsInstant = DateUtils.localDateToInstant(date);

            final var expected_1 = createNewExchangeRate(currency, dateAsInstant);
            final var rate_1 = expected_1.getRateByProvider(PROVIDER_NAME);

            final var expected_2 = createNewExchangeRate(currency, dateAsInstant);
            final var rate_2 = expected_2.getRateByProvider(PROVIDER_NAME);

            // when
            when(repository.findByCurrency(eq(currency), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1, expected_2)));

            // then
            mockMvc.perform(get(BASE_URL + "?pageNumber=" + 0 + "&pageSize=" + 1))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currencies", is(expectedContentPerPage)))
                    .andExpect(jsonPath("$.exchangeRates", is(expectedTotalContentSize)))
                    .andExpect(jsonPath("$.totalExchangeRates", is(expectedTotalContentSize)))

                    .andExpect(jsonPath("$.content[0].numberOfElements", is(expectedTotalContentSize)))
                    .andExpect(jsonPath("$.content[0].totalElements", is(expectedTotalContentSize)))
                    .andExpect(jsonPath("$.content[0].totalPages", is(expectedContentPerPage)))
                    .andExpect(jsonPath("$.content[0].content.currency", is(currency.toString())))
                    .andExpect(jsonPath("$.content[0].content.rates", hasSize(expectedTotalContentSize)))

                    .andExpect(jsonPath("$.content[0].content.rates[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.from.currency", is(rate_1.getFrom().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.from.value", is(rate_1.getFrom().getValue())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.to.currency", is(rate_1.getTo().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.to.value", is(rate_1.getTo().getValue())))

                    .andExpect(jsonPath("$.content[0].content.rates[1].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.from.currency", is(rate_2.getFrom().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.from.value", is(rate_2.getFrom().getValue())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.to.currency", is(rate_2.getTo().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.to.value", is(rate_2.getTo().getValue())));
        }

        @Test
        void shouldReturnBadRequest_whenPageSizeIsMissed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?pageNumber=" + 0))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPageNumberIsMissed() throws Exception {
            mockMvc.perform(get(BASE_URL + "?pageSize=" + 0))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenDateAndDateRangeArePassed() throws Exception {
            final var date = LocalDate.now();
            mockMvc.perform(get(BASE_URL + "?date=" + date + "&from=" + date + "&to=" + date))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenDateAndDateFromArePassed() throws Exception {
            final var date = LocalDate.now();
            mockMvc.perform(get(BASE_URL + "?date=" + date + "&from=" + date))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenDateAndDateToArePassed() throws Exception {
            final var date = LocalDate.now();
            mockMvc.perform(get(BASE_URL + "?date=" + date + "&to=" + date))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class GetLatest {

        @Test
        void shouldRetrieveLatestExchangeRates() throws Exception {
            // given
            final var expectedContentSize = 1;
            final var expectedPagesSize = 2;

            final var date = LocalDate.now().minusDays(5);
            final var dateAsInstant = DateUtils.localDateToInstant(date);

            final var currency_1 = CurrencyCode.CHF;
            final var expected_1 = createNewExchangeRate(currency_1, dateAsInstant);
            final var rate_1 = expected_1.getRateByProvider(PROVIDER_NAME);

            final var currency_2 = CurrencyCode.JPY;
            final var expected_2 = createNewExchangeRate(currency_2, dateAsInstant);
            final var rate_2 = expected_2.getRateByProvider(PROVIDER_NAME);

            // when
            when(repository.findByDate(any(Instant.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1, expected_2)));

            // then
            mockMvc.perform(get(latestUrl()))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currencies", is(expectedPagesSize)))

                    .andExpect(jsonPath("$.content[0].currency", is(currency_1.toString())))
                    .andExpect(jsonPath("$.content[0].rates", hasSize(expectedContentSize)))
                    .andExpect(jsonPath("$.content[0].rates[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].rates[0].rate.from.currency", is(rate_1.getFrom().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].rates[0].rate.from.value", is(rate_1.getFrom().getValue())))
                    .andExpect(jsonPath("$.content[0].rates[0].rate.to.currency", is(rate_1.getTo().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].rates[0].rate.to.value", is(rate_1.getTo().getValue())))

                    .andExpect(jsonPath("$.content[1].currency", is(currency_2.toString())))
                    .andExpect(jsonPath("$.content[1].rates", hasSize(expectedContentSize)))
                    .andExpect(jsonPath("$.content[1].rates[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[1].rates[0].rate.from.currency", is(rate_2.getFrom().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[1].rates[0].rate.from.value", is(rate_2.getFrom().getValue())))
                    .andExpect(jsonPath("$.content[1].rates[0].rate.to.currency", is(rate_2.getTo().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[1].rates[0].rate.to.value", is(rate_2.getTo().getValue())));
        }

    }

    @Nested
    class GetForCurrency {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldRetrieveAllExchangeRatesForCurrency(final CurrencyCode currency) throws Exception {
            // given
            final var expectedContentSize = 2;
            final var expectedPagesSize = 1;

            final var date = LocalDate.now().minusDays(3);
            final var dateAsInstant = DateUtils.localDateToInstant(date);

            final var expected_1 = createNewExchangeRate(currency, dateAsInstant);
            final var rate_1 = expected_1.getRateByProvider(PROVIDER_NAME);

            final var expected_2 = createNewExchangeRate(currency, dateAsInstant);
            final var rate_2 = expected_2.getRateByProvider(PROVIDER_NAME);

            // when
            when(repository.findByCurrency(eq(currency), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1, expected_2)));

            // then
            mockMvc.perform(get(forCurrencyUrl(currency)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currencies", is(expectedPagesSize)))
                    .andExpect(jsonPath("$.exchangeRates", is(expectedContentSize)))
                    .andExpect(jsonPath("$.totalExchangeRates", is(expectedContentSize)))

                    .andExpect(jsonPath("$.content[0].numberOfElements", is(expectedContentSize)))
                    .andExpect(jsonPath("$.content[0].totalElements", is(expectedContentSize)))
                    .andExpect(jsonPath("$.content[0].totalPages", is(expectedPagesSize)))
                    .andExpect(jsonPath("$.content[0].content.currency", is(currency.toString())))
                    .andExpect(jsonPath("$.content[0].content.rates", hasSize(expectedContentSize)))

                    .andExpect(jsonPath("$.content[0].content.rates[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.from.currency", is(rate_1.getFrom().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.from.value", is(rate_1.getFrom().getValue())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.to.currency", is(rate_1.getTo().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.to.value", is(rate_1.getTo().getValue())))

                    .andExpect(jsonPath("$.content[0].content.rates[1].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.from.currency", is(rate_2.getFrom().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.from.value", is(rate_2.getFrom().getValue())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.to.currency", is(rate_2.getTo().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.to.value", is(rate_2.getTo().getValue())));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldRetrieveExchangeRateForCurrency_whenDateRangeIsPassed(final CurrencyCode currency) throws Exception {
            // given
            final var expectedContentSize = 2;
            final var expectedPagesSize = 1;

            final var date = LocalDate.now().minusDays(3);
            final var dateAsInstant = DateUtils.localDateToInstant(date);
            final var dateFrom = date.minusDays(2);
            final var dateTo = date.plusDays(1);

            final var expected_1 = createNewExchangeRate(currency, dateAsInstant);
            final var rate_1 = expected_1.getRateByProvider(PROVIDER_NAME);

            final var expected_2 = createNewExchangeRate(currency, dateAsInstant);
            final var rate_2 = expected_2.getRateByProvider(PROVIDER_NAME);

            // when
            when(repository.findByCurrencyAndDateBetween(eq(currency), any(Instant.class), any(Instant.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1, expected_2)));

            // then
            mockMvc.perform(get(forCurrencyUrl(currency) + "?from=" + dateFrom + "&to=" + dateTo))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currencies", is(expectedPagesSize)))
                    .andExpect(jsonPath("$.exchangeRates", is(expectedContentSize)))
                    .andExpect(jsonPath("$.totalExchangeRates", is(expectedContentSize)))

                    .andExpect(jsonPath("$.content[0].numberOfElements", is(expectedContentSize)))
                    .andExpect(jsonPath("$.content[0].totalElements", is(expectedContentSize)))
                    .andExpect(jsonPath("$.content[0].totalPages", is(expectedPagesSize)))
                    .andExpect(jsonPath("$.content[0].content.currency", is(currency.toString())))
                    .andExpect(jsonPath("$.content[0].content.rates", hasSize(expectedContentSize)))

                    .andExpect(jsonPath("$.content[0].content.rates[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.from.currency", is(rate_1.getFrom().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.from.value", is(rate_1.getFrom().getValue())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.to.currency", is(rate_1.getTo().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[0].rate.to.value", is(rate_1.getTo().getValue())))

                    .andExpect(jsonPath("$.content[0].content.rates[1].date", is(date.toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.from.currency", is(rate_2.getFrom().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.from.value", is(rate_2.getFrom().getValue())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.to.currency", is(rate_2.getTo().getCurrency().toString())))
                    .andExpect(jsonPath("$.content[0].content.rates[1].rate.to.value", is(rate_2.getTo().getValue())));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldReturnBadRequest_whenDateToIsMissed(final CurrencyCode currency) throws Exception {
            final var date = LocalDate.now().minusDays(3);
            mockMvc.perform(get(forCurrencyUrl(currency) + "?from=" + date))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldReturnBadRequest_whenDateFromIsMissed(final CurrencyCode currency) throws Exception {
            final var date = LocalDate.now().minusDays(3);
            mockMvc.perform(get(forCurrencyUrl(currency) + "?to=" + date))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    class GetLatestForCurrency {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldRetrieveLatestExchangeRateForCurrency(final CurrencyCode currency) throws Exception {
            // given
            final var expectedContentSize = 1;

            final var date = LocalDate.now();
            final var dateAsInstant = DateUtils.localDateToInstant(date);

            final var expected_1 = createNewExchangeRate(currency, dateAsInstant);
            final var rate_1 = expected_1.getRateByProvider(PROVIDER_NAME);

            // when
            when(repository.findByCurrencyAndDate(eq(currency), any(Instant.class))).thenReturn(Optional.of(expected_1));

            // then
            mockMvc.perform(get(latestForCurrencyUrl(currency)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.currency", is(currency.toString())))
                    .andExpect(jsonPath("$.rates", hasSize(expectedContentSize)))
                    .andExpect(jsonPath("$.rates[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.rates[0].rate.from.currency", is(rate_1.getFrom().getCurrency().toString())))
                    .andExpect(jsonPath("$.rates[0].rate.from.value", is(rate_1.getFrom().getValue())))
                    .andExpect(jsonPath("$.rates[0].rate.to.currency", is(rate_1.getTo().getCurrency().toString())))
                    .andExpect(jsonPath("$.rates[0].rate.to.value", is(rate_1.getTo().getValue())));
        }

        @Test
        void shouldReturnBadRequest_whenUnknownCurrencyWasPassed() throws Exception {
            mockMvc.perform(get(latestForCurrencyUrl(CurrencyCode.UNDEFINED)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldReturnNotFound_whenLatestExchangeRateDoesNotExistsForCurrency(final CurrencyCode currency) throws Exception {
            // given && when
            when(repository.findByCurrencyAndDate(eq(currency), any(Instant.class))).thenReturn(Optional.empty());

            // then
            mockMvc.perform(get(latestForCurrencyUrl(currency)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    class GetForCurrencyAndDate {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldRetrieveExchangeRateForCurrencyAndSpecificDate(final CurrencyCode currency) throws Exception {
            // given
            final var expectedContentSize = 1;

            final var date = LocalDate.now().minusDays(8);
            final var dateAsInstant = DateUtils.localDateToInstant(date);

            final var expected_1 = createNewExchangeRate(currency, dateAsInstant);
            final var rate_1 = expected_1.getRateByProvider(PROVIDER_NAME);

            // when
            when(repository.findByCurrencyAndDate(eq(currency), any(Instant.class))).thenReturn(Optional.of(expected_1));

            // then
            mockMvc.perform(get(forCurrencyAndDateUrl(currency, date)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$.currency", is(currency.toString())))
                    .andExpect(jsonPath("$.rates", hasSize(expectedContentSize)))
                    .andExpect(jsonPath("$.rates[0].date", is(date.toString())))
                    .andExpect(jsonPath("$.rates[0].rate.from.currency", is(rate_1.getFrom().getCurrency().toString())))
                    .andExpect(jsonPath("$.rates[0].rate.from.value", is(rate_1.getFrom().getValue())))
                    .andExpect(jsonPath("$.rates[0].rate.to.currency", is(rate_1.getTo().getCurrency().toString())))
                    .andExpect(jsonPath("$.rates[0].rate.to.value", is(rate_1.getTo().getValue())));
        }

        @Test
        void shouldReturnBadRequest_whenUnknownCurrencyWasPassed() throws Exception {
            final var date = LocalDate.now().minusDays(3);
            mockMvc.perform(get(forCurrencyAndDateUrl(CurrencyCode.UNDEFINED, date)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldReturnNotFound_whenExchangeRateDoesNotExistsForCurrencyAndDate(final CurrencyCode currency) throws Exception {
            // given && when
            final var date = LocalDate.now().minusDays(3);
            when(repository.findByCurrencyAndDate(eq(currency), any(Instant.class))).thenReturn(Optional.empty());

            // then
            mockMvc.perform(get(forCurrencyAndDateUrl(currency, date)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

    }

    private String latestUrl() {
        return BASE_URL + "/latest";
    }

    private String latestForCurrencyUrl(final CurrencyCode currency) {
        return BASE_URL + "/" + currency + "/latest";
    }

    private String forCurrencyUrl(final CurrencyCode currency) {
        return BASE_URL + "/" + currency;
    }

    private String forCurrencyAndDateUrl(final CurrencyCode currency, final LocalDate date) {
        return BASE_URL + "/" + currency + "/" + date;
    }

}