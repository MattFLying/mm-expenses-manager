package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.pageable.PageHelper;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exception.ExchangeRateException;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mm.expenses.manager.finance.exchangerate.ExchangeRateAssert.assertExchangeRate;
import static mm.expenses.manager.finance.exchangerate.ExchangeRateHelper.createNewExchangeRate;
import static mm.expenses.manager.finance.exchangerate.ExchangeRateHelper.currencyRateToExchangeRate;
import static mm.expenses.manager.finance.exchangerate.ExchangeRatesAssert.assertExchangeRates;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExchangeRateServiceTest extends FinanceApplicationTest {

    @MockBean
    private CurrencyRatesConfig currencyRatesConfig;

    @MockBean
    private ExchangeRateRepository exchangeRateRepository;

    @MockBean
    private ExchangeRateHistoryUpdate exchangeRateHistoryUpdate;

    @Autowired
    private ExchangeRateService exchangeRateService;


    @Override
    protected void setupBeforeEachTest() {
        when(currencyRatesConfig.getDefaultCurrency()).thenReturn(DEFAULT_CURRENCY);
        when(currencyRatesConfig.getAllRequiredCurrenciesCode()).thenCallRealMethod();
    }

    @Override
    protected void setupAfterEachTest() {
        reset(currencyRatesConfig);
        reset(exchangeRateRepository);
        reset(exchangeRateHistoryUpdate);
    }


    @Test
    void shouldGetPageRequest() {
        // given
        final var pageNumber = 0;
        final var pageSize = 5;

        // when
        final var result = exchangeRateService.pageRequest(pageNumber, pageSize);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPageNumber()).isEqualTo(pageNumber);
        assertThat(result.getPageSize()).isEqualTo(pageSize);
        assertThat(result.getSort().isSorted()).isTrue();
        assertThat(result.getSort()).hasToString(ExchangeRate.DEFAULT_SORT_BY + ": " + ExchangeRate.DEFAULT_SORT_ORDER.getDirection());
    }

    @Test
    void shouldRetrieveAllCurrencyCodesWithoutUndefinedAndDefaultCurrentlyUsed() {
        // given && when
        final var result = currencyRatesConfig.getAllRequiredCurrenciesCode();

        // then
        assertThat(result).isNotNull().containsExactlyInAnyOrderElementsOf(Stream.of(CurrencyCode.values()).filter(code -> !code.equals(CurrencyCode.UNDEFINED) || !code.equals(DEFAULT_CURRENCY)).collect(Collectors.toSet()));
    }

    @Nested
    class FindToday {

        @Test
        void shouldFindTodayRates() {
            // given
            final var today = DateUtils.localDateToInstantUTC(LocalDate.now());
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.AUD;
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.JPY;
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, today, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, today, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateRepository.findByDate(eq(today), any(Pageable.class))).thenReturn(new PageImpl<>(expectedList));

            final var result = exchangeRateService.findToday();

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency_1, expected_1)
                    .forCurrencyHasExactlyTheSameAs(currency_2, expected_2);
        }

    }

    @Nested
    class FindForCurrencyAndSpecificDate {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindForCurrencyAndSpecificDate(final CurrencyCode currency) {
            // given
            final var id = UUID.randomUUID().toString();
            final var date = DateUtils.localDateToInstantUTC(LocalDate.of(2011, 5, 20));
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusMonths(6));
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);
            final Map<String, Object> details = Map.of();

            final var expected = createNewExchangeRate(id, currency, date, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            // when
            when(exchangeRateRepository.findByCurrencyAndDate(currency, date)).thenReturn(Optional.of(expected));

            final var result = exchangeRateService.findForCurrencyAndSpecificDate(currency, DateUtils.instantToLocalDateUTC(date));

            // then
            assertExchangeRate(result)
                    .hasId(id)
                    .isOfCurrency(currency)
                    .isOfDate(date)
                    .createdAt(createdModified)
                    .modifiedAt(createdModified)
                    .hasRateForProvider(PROVIDER_NAME, rate)
                    .hasDetailsForProvider(PROVIDER_NAME, details)
                    .hasVersion(ExchangeRateHelper.INITIAL_VERSION);
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldReturnEmpty_whenDoesNotExists(final CurrencyCode currency) {
            // given
            final var date = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(3));
            when(exchangeRateRepository.findByCurrencyAndDate(currency, date)).thenReturn(Optional.empty());

            // when
            final var result = exchangeRateService.findForCurrencyAndSpecificDate(currency, DateUtils.instantToLocalDateUTC(date));

            // then
            assertThat(result).isEmpty();
        }

    }

    @Nested
    class FindForCurrencyCodesAndSpecificDate {

        @Test
        void shouldFindForCurrencyCodesAndSpecificDate() {
            // given
            final var id = UUID.randomUUID().toString();
            final var currency = CurrencyCode.SEK;
            final var date = DateUtils.localDateToInstantUTC(LocalDate.of(2011, 5, 20));
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusMonths(6));
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);
            final Map<String, Object> details = Map.of();

            final var expected = createNewExchangeRate(id, currency, date, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            // when
            when(exchangeRateRepository.findByCurrencyAndDate(currency, date)).thenReturn(Optional.of(expected));

            final var result = exchangeRateService.findForCurrencyCodesAndSpecificDate(Set.of(currency), DateUtils.instantToLocalDateUTC(date), PageHelper.getPageRequest(0, 10));

            // then
            assertExchangeRates(result).forCurrencyHasExactlyTheSameAs(currency, List.of(expected));
        }

    }

    @Nested
    class FindByDate {

        @Test
        void shouldFindByDate() {
            // given
            final var id = UUID.randomUUID().toString();
            final var date = DateUtils.localDateToInstantUTC(LocalDate.of(2011, 5, 20));
            final var currency = CurrencyCode.SEK;
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusMonths(6));
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);
            final Map<String, Object> details = Map.of();

            final var expected = createNewExchangeRate(id, currency, date, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            // when
            when(exchangeRateRepository.findByDate(eq(date), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected)));

            final var result = exchangeRateService.findByDate(PageHelper.getPageRequest(0, 1), DateUtils.instantToLocalDateUTC(date));

            // then
            assertExchangeRates(result).forCurrencyHasExactlyTheSameAs(currency, List.of(expected));
        }

    }

    @Nested
    class FindAllForCurrency {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindAllForCurrency_whenDatesWereNotPassed(final CurrencyCode currency) {
            // given
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(7));
            final Map<String, Object> details = Map.of();

            final var date_1 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(17));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var date_2 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(20));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var expected_1 = createNewExchangeRate(id_1, currency, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateRepository.findByCurrency(eq(currency), any(Pageable.class))).thenReturn(new PageImpl<>(expectedList));

            final var result = exchangeRateService.findAllForCurrency(currency, null, null, PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency, expectedList);
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindAllForCurrency_whenDatesWerePassed(final CurrencyCode currency) {
            // given
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(11));
            final Map<String, Object> details = Map.of();

            final var date_1 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(15));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var date_2 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var expected_1 = createNewExchangeRate(id_1, currency, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateRepository.findByCurrencyAndDateBetween(eq(currency), eq(date_1), eq(date_2), any(Pageable.class))).thenReturn(new PageImpl<>(expectedList));

            final var result = exchangeRateService.findAllForCurrency(currency, DateUtils.instantToLocalDateUTC(date_1), DateUtils.instantToLocalDateUTC(date_2), PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency, expectedList);
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindAllForCurrency_whenDateFromWasNotPassed(final CurrencyCode currency) {
            // given
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(11));
            final Map<String, Object> details = Map.of();

            final var date_1 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(15));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var date_2 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var expected_1 = createNewExchangeRate(id_1, currency, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateRepository.findByCurrency(eq(currency), any(Pageable.class))).thenReturn(new PageImpl<>(expectedList));

            final var result = exchangeRateService.findAllForCurrency(currency, null, DateUtils.instantToLocalDateUTC(date_2), PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency, expectedList);
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindAllForCurrency_whenDateToWasNotPassed(final CurrencyCode currency) {
            // given
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(11));
            final Map<String, Object> details = Map.of();

            final var date_1 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(15));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var date_2 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var expected_1 = createNewExchangeRate(id_1, currency, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateRepository.findByCurrency(eq(currency), any(Pageable.class))).thenReturn(new PageImpl<>(expectedList));

            final var result = exchangeRateService.findAllForCurrency(currency, DateUtils.instantToLocalDateUTC(date_1), null, PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency, expectedList);
        }

    }

    @Nested
    class FindAll {

        @Test
        void shouldFindAll_whenNoDatesWerePassed() {
            // given
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(3));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.CAD;
            final var date_1 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(7));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.NZD;
            final var date_2 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_2);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateRepository.findByCurrency(eq(currency_1), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1)));
            when(exchangeRateRepository.findByCurrency(eq(currency_2), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_2)));

            final var result = exchangeRateService.findAll(null, null, null, PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency_1, List.of(expected_1))
                    .forCurrencyHasExactlyTheSameAs(currency_2, List.of(expected_2));
        }

        @Test
        void shouldFindAll_whenDateFromWasNotPassed() {
            // given
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(3));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.GBP;
            final var date_1 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(7));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.JPY;
            final var date_2 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_2);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateRepository.findByCurrency(eq(currency_1), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1)));
            when(exchangeRateRepository.findByCurrency(eq(currency_2), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_2)));

            final var result = exchangeRateService.findAll(null, null, DateUtils.instantToLocalDateUTC(date_1), PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency_1, List.of(expected_1))
                    .forCurrencyHasExactlyTheSameAs(currency_2, List.of(expected_2));
        }

        @Test
        void shouldFindAll_whenDateToWasNotPassed() {
            // given
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(3));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.CHF;
            final var date_1 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(7));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.AUD;
            final var date_2 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_2);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateRepository.findByCurrency(eq(currency_1), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1)));
            when(exchangeRateRepository.findByCurrency(eq(currency_2), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_2)));

            final var result = exchangeRateService.findAll(null, DateUtils.instantToLocalDateUTC(date_2), null, PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency_1, List.of(expected_1))
                    .forCurrencyHasExactlyTheSameAs(currency_2, List.of(expected_2));
        }

        @Test
        void shouldFindAll_whenDateWasPassed() {
            // given
            final var date = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(3));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.CAD;
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.JPY;
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_2);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, date, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, date, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateRepository.findByCurrencyAndDate(eq(currency_1), eq(date))).thenReturn(Optional.of(expected_1));
            when(exchangeRateRepository.findByCurrencyAndDate(eq(currency_2), eq(date))).thenReturn(Optional.of(expected_2));

            final var result = exchangeRateService.findAll(DateUtils.instantToLocalDateUTC(date), null, null, PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency_1, List.of(expected_1))
                    .forCurrencyHasExactlyTheSameAs(currency_2, List.of(expected_2));
        }

        @Test
        void shouldFindAll_whenDatesRangeWasPassed() {
            // given
            final var date = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(3));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.SEK;
            final var date_1 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(7));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.AUD;
            final var date_2 = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_2);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, date, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, date, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateRepository.findByCurrencyAndDateBetween(eq(currency_1), eq(date_2), eq(date_1), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1)));
            when(exchangeRateRepository.findByCurrencyAndDateBetween(eq(currency_2), eq(date_2), eq(date_1), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_2)));

            final var result = exchangeRateService.findAll(null, DateUtils.instantToLocalDateUTC(date_2), DateUtils.instantToLocalDateUTC(date_1), PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency_1, List.of(expected_1))
                    .forCurrencyHasExactlyTheSameAs(currency_2, List.of(expected_2));
        }

    }

    @Nested
    class CreateOrUpdate {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldCreateNewExchangeRate(final CurrencyCode currency) {
            // given
            final var dateNow = Instant.now();

            final var date = LocalDate.now().minusDays(9);
            final var rate = 5.8d;
            final var tableNumber = "test/table/number/02";

            final var currencyRateToSave = createCurrencyRate(currency, date, rate, TABLE_TYPE, tableNumber);
            final var currencyRatesSaved = currencyRateToExchangeRate(currencyRateToSave, dateNow);

            // when
            when(exchangeRateRepository.saveAll(anyCollection())).thenReturn(List.of(currencyRatesSaved));

            final var resultCollection = exchangeRateService.createOrUpdate(List.of(currencyRateToSave));

            // then
            assertThat(resultCollection).isNotNull().hasSize(1);
            assertThat(resultCollection.stream().findAny()).isNotEmpty();
            final var result = resultCollection.stream().findAny().get();
            assertExchangeRate(result)
                    .hasId(ExchangeRateHelper.ID)
                    .isOfCurrency(currency)
                    .isOfDate(DateUtils.localDateToInstantUTC(date))
                    .createdAt(dateNow)
                    .modifiedAt(dateNow)
                    .hasRates(currencyRatesSaved.getRatesByProvider())
                    .hasDetails(currencyRatesSaved.getDetailsByProvider())
                    .hasVersion(ExchangeRateHelper.INITIAL_VERSION);
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldNotUpdateAnything_whenExchangeRateExistsWithTheSameProvider(final CurrencyCode currency) {
            // given
            final var dateNow = Instant.now();

            final var date = LocalDate.now().minusDays(3);
            final var rate = 2.7d;
            final var tableNumber = "test/table/number/03";

            final var currencyRateToSave = createCurrencyRate(currency, date, rate, TABLE_TYPE, tableNumber);
            final var currencyRatesSaved = currencyRateToExchangeRate(currencyRateToSave, dateNow);

            // when
            when(exchangeRateRepository.findByCurrencyInAndDate(anySet(), any(Instant.class))).thenReturn(Stream.of(currencyRatesSaved));
            when(exchangeRateRepository.saveAll(anyCollection())).thenReturn(List.of(currencyRatesSaved));

            final var resultCollection = exchangeRateService.createOrUpdate(List.of(currencyRateToSave));

            // then
            assertThat(resultCollection).isNotNull().hasSize(1);
            assertThat(resultCollection.stream().findAny()).isNotEmpty();
            final var result = resultCollection.stream().findAny().get();
            assertExchangeRate(result)
                    .hasId(ExchangeRateHelper.ID)
                    .isOfCurrency(currency)
                    .isOfDate(DateUtils.localDateToInstantUTC(date))
                    .createdAt(dateNow)
                    .modifiedAt(dateNow)
                    .hasRates(currencyRatesSaved.getRatesByProvider())
                    .hasDetails(currencyRatesSaved.getDetailsByProvider())
                    .hasVersion(ExchangeRateHelper.INITIAL_VERSION);
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldUpdate_whenExchangeRateExistsWithTheSameProvider(final CurrencyCode currency) {
            // given
            final var dateNow = Instant.now();

            final var date = LocalDate.now().minusDays(7);
            final var rate = 3.7d;
            final var tableNumber = "test/table/number/04";

            final var currencyRateToSave = createCurrencyRate(currency, date, rate, TABLE_TYPE, tableNumber);
            final var currencyRatesSaved = currencyRateToExchangeRate(currencyRateToSave, dateNow);
            final var currencyRatesExistedWithoutProvider = currencyRatesSaved.toBuilder().ratesByProvider(new HashMap<>()).build();

            // when
            when(exchangeRateRepository.findByCurrencyInAndDate(anySet(), any(Instant.class))).thenReturn(Stream.of(currencyRatesExistedWithoutProvider));
            when(exchangeRateRepository.saveAll(anyCollection())).thenReturn(List.of(currencyRatesSaved));

            final var resultCollection = exchangeRateService.createOrUpdate(List.of(currencyRateToSave));

            // then
            assertThat(resultCollection).isNotNull().hasSize(1);
            assertThat(resultCollection.stream().findAny()).isNotEmpty();
            final var result = resultCollection.stream().findAny().get();
            assertExchangeRate(result)
                    .hasId(ExchangeRateHelper.ID)
                    .isOfCurrency(currency)
                    .isOfDate(DateUtils.localDateToInstantUTC(date))
                    .createdAt(dateNow)
                    .modifiedAt(dateNow)
                    .hasRates(currencyRatesSaved.getRatesByProvider())
                    .hasDetails(currencyRatesSaved.getDetailsByProvider())
                    .hasVersion(ExchangeRateHelper.INITIAL_VERSION);
        }

        @Test
        void shouldCreateAndUpdate() {
            // given
            final var dateNow = Instant.now();

            final var currency_1 = CurrencyCode.AUD;
            final var date_1 = LocalDate.now().minusDays(8);
            final var rate_1 = 6.5d;
            final var tableNumber_1 = "test/table/number/04";

            final var currency_2 = CurrencyCode.GBP;
            final var date_2 = LocalDate.now().minusDays(5);
            final var rate_2 = 3.8d;
            final var tableNumber_2 = "test/table/number/04";

            final var date_3 = LocalDate.now().minusDays(3);
            final var rate_3 = 7.8d;
            final var tableNumber_3 = "test/table/number/05";

            final var currencyRateToSave_1 = createCurrencyRate(currency_1, date_1, rate_1, TABLE_TYPE, tableNumber_1);
            final var currencyRatesSaved_1 = currencyRateToExchangeRate(currencyRateToSave_1, dateNow);
            final var currencyRatesExistedWithoutProvider_1 = currencyRatesSaved_1.toBuilder().ratesByProvider(new HashMap<>()).build();

            final var currencyRateToSave_2 = createCurrencyRate(currency_2, date_2, rate_2, TABLE_TYPE, tableNumber_2);
            final var currencyRatesSaved_2 = currencyRateToExchangeRate(currencyRateToSave_2, dateNow);

            final var currencyRateToSave_3 = createCurrencyRate(currency_1, date_3, rate_3, TABLE_TYPE, tableNumber_3);
            final var currencyRatesSaved_3 = currencyRateToExchangeRate(currencyRateToSave_3, dateNow);
            final var expectedResult = List.of(currencyRatesSaved_1, currencyRatesSaved_2, currencyRatesSaved_3);

            // when
            when(exchangeRateRepository.findByCurrencyInAndDateBetween(anySet(), any(Instant.class), any(Instant.class))).thenReturn(Stream.of(currencyRatesExistedWithoutProvider_1));
            when(exchangeRateRepository.saveAll(anyCollection())).thenReturn(expectedResult);

            final var result = exchangeRateService.createOrUpdate(List.of(currencyRateToSave_1, currencyRateToSave_2, currencyRateToSave_3));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedResult)
                    .forCurrencyHasExactlyTheSameAs(currency_1, List.of(currencyRatesSaved_1, currencyRatesSaved_3))
                    .forCurrencyHasExactlyTheSameAs(currency_2, List.of(currencyRatesSaved_2));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldThrowExchangeRateException_whenDatesAreIncorrect(final CurrencyCode currency) {
            // given
            final var rate = 5.8d;
            final var tableNumber = "test/table/number/02";

            final var currencyRateToSave = createCurrencyRate(currency, null, rate, TABLE_TYPE, tableNumber);

            // when && then
            assertThatThrownBy(() -> exchangeRateService.createOrUpdate(List.of(currencyRateToSave)))
                    .isInstanceOf(ExchangeRateException.class)
                    .hasMessage(FinanceExceptionMessage.EXCHANGE_RATES_INVALID_DATE_TO.getMessage());
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldThrowExchangeRateException_whenSomethingWentWrong(final CurrencyCode currency) {
            // given
            final var date = LocalDate.now().minusDays(5);
            final var rate = 5.8d;
            final var tableNumber = "test/table/number/02";

            final var currencyRateToSave = createCurrencyRate(currency, date, rate, TABLE_TYPE, tableNumber);

            when(exchangeRateRepository.findByCurrencyInAndDate(anySet(), any(Instant.class))).thenThrow(RuntimeException.class);

            // when && then
            assertThatThrownBy(() -> exchangeRateService.createOrUpdate(List.of(currencyRateToSave)))
                    .isInstanceOf(ExchangeRateException.class)
                    .hasMessage(FinanceExceptionMessage.SAVE_OR_UPDATE_EXCHANGE_RATES.getMessage());
        }

    }

    @Nested
    class Synchronize {

        @Test
        void shouldSynchronizeExchangeRate() {
            // given
            final var dateNow = Instant.now();

            final var currency = CurrencyCode.GBP;
            final var date = LocalDate.now().minusDays(4);
            final var rate = 3.3d;
            final var tableNumber = "test/table/number/05";

            final var currencyRateToSave = createCurrencyRate(currency, date, rate, TABLE_TYPE, tableNumber);
            final var currencyRatesSaved = currencyRateToExchangeRate(currencyRateToSave, dateNow);

            // when
            when(exchangeRateRepository.saveAll(anyCollection())).thenReturn(List.of(currencyRatesSaved));

            final var resultCollection = exchangeRateService.synchronize(List.of(currencyRateToSave));

            // then
            assertThat(resultCollection).isNotNull().hasSize(1);
            assertThat(resultCollection.stream().findAny()).isNotEmpty();
            final var result = resultCollection.stream().findAny().get();
            assertExchangeRate(result)
                    .hasId(ExchangeRateHelper.ID)
                    .isOfCurrency(currency)
                    .isOfDate(DateUtils.localDateToInstantUTC(date))
                    .createdAt(dateNow)
                    .modifiedAt(dateNow)
                    .hasRates(currencyRatesSaved.getRatesByProvider())
                    .hasDetails(currencyRatesSaved.getDetailsByProvider())
                    .hasVersion(ExchangeRateHelper.INITIAL_VERSION);
        }

        @Test
        void shouldThrowExchangeRateException_whenDatesAreIncorrect() {
            // given
            final var currency = CurrencyCode.AUD;
            final var rate = 2.8d;
            final var tableNumber = "test/table/number/07";

            final var currencyRateToSave = createCurrencyRate(currency, null, rate, TABLE_TYPE, tableNumber);

            // when && then
            assertThatThrownBy(() -> exchangeRateService.synchronize(List.of(currencyRateToSave)))
                    .isInstanceOf(ExchangeRateException.class)
                    .hasMessage(FinanceExceptionMessage.EXCHANGE_RATES_INVALID_DATE_TO.getMessage());
        }

        @Test
        void shouldThrowExchangeRateException_whenSomethingWentWrong() {
            // given
            final var currency = CurrencyCode.SEK;
            final var date = LocalDate.now().minusDays(7);
            final var rate = 4.3d;
            final var tableNumber = "test/table/number/08";

            final var currencyRateToSave = createCurrencyRate(currency, date, rate, TABLE_TYPE, tableNumber);

            when(exchangeRateRepository.findByCurrencyInAndDate(anySet(), any(Instant.class))).thenThrow(RuntimeException.class);

            // when && then
            assertThatThrownBy(() -> exchangeRateService.synchronize(List.of(currencyRateToSave)))
                    .isInstanceOf(ExchangeRateException.class)
                    .hasMessage(FinanceExceptionMessage.SAVE_OR_UPDATE_EXCHANGE_RATES.getMessage());
        }

    }

    @Nested
    class HistoryUpdate {

        @Test
        void historyUpdateIsCalled() {
            // given && when
            exchangeRateService.historyUpdate();

            // then
            await().untilAsserted(() -> verify(exchangeRateHistoryUpdate).update());
        }

    }

}