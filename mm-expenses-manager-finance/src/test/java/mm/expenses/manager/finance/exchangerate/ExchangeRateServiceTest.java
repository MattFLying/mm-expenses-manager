package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.exception.InvalidDateException;
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
import java.util.stream.Stream;

import static mm.expenses.manager.finance.exchangerate.ExchangeRateAssert.assertExchangeRate;
import static mm.expenses.manager.finance.exchangerate.ExchangeRateHelper.createNewExchangeRate;
import static mm.expenses.manager.finance.exchangerate.ExchangeRateHelper.currencyRateToExchangeRate;
import static mm.expenses.manager.finance.exchangerate.ExchangeRatesAssert.assertExchangeRates;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExchangeRateServiceTest extends FinanceApplicationTest {

    @MockBean
    private CurrencyRatesConfig config;

    @MockBean
    private ExchangeRateRepository repository;

    @MockBean
    private ExchangeRateHistoryUpdate historyUpdate;

    @Autowired
    private ExchangeRateService service;

    @Override
    protected void setupBeforeEachTest() {
        when(config.getDefaultCurrency()).thenReturn(CurrencyCode.PLN);
    }

    @Override
    protected void setupAfterEachTest() {
        reset(config);
        reset(repository);
        reset(historyUpdate);
    }

    @Nested
    class FindLatestForCurrency {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindLatestForCurrency(final CurrencyCode currency) {
            // given
            final var id = UUID.randomUUID().toString();
            final var today = DateUtils.localDateToInstant(LocalDate.now());
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(5));
            final var rate = ExchangeRateHelper.createNewRateToPLN(currency, 3.3);
            final Map<String, Object> details = Map.of();

            final var expected = createNewExchangeRate(id, currency, today, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            // when
            when(repository.findByCurrencyAndDate(currency, today)).thenReturn(Optional.of(expected));

            final var result = service.findLatestForCurrency(currency);

            // then
            assertExchangeRate(result)
                    .hasId(id)
                    .isOfCurrency(currency)
                    .isOfDate(today)
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
            final var today = DateUtils.localDateToInstant(LocalDate.now());
            when(repository.findByCurrencyAndDate(currency, today)).thenReturn(Optional.empty());

            // when
            final var result = service.findLatestForCurrency(currency);

            // then
            assertThat(result).isEmpty();
        }

    }

    @Nested
    class FindLatest {

        @Test
        void shouldFindLatest() {
            // given
            final var today = DateUtils.localDateToInstant(LocalDate.now());
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.AUD;
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRateToPLN(currency_1, 3.3);

            final var currency_2 = CurrencyCode.JPY;
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRateToPLN(currency_1, 1.45);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, today, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, today, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(repository.findByDate(eq(today), any(Pageable.class))).thenReturn(new PageImpl<>(expectedList));

            final var result = service.findLatest();

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
            final var date = DateUtils.localDateToInstant(LocalDate.of(2011, 5, 20));
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusMonths(6));
            final var rate = ExchangeRateHelper.createNewRateToPLN(currency, 6.7);
            final Map<String, Object> details = Map.of();

            final var expected = createNewExchangeRate(id, currency, date, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            // when
            when(repository.findByCurrencyAndDate(currency, date)).thenReturn(Optional.of(expected));

            final var result = service.findForCurrencyAndSpecificDate(currency, DateUtils.instantToLocalDateUTC(date));

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
            final var date = DateUtils.localDateToInstant(LocalDate.now().minusDays(3));
            when(repository.findByCurrencyAndDate(currency, date)).thenReturn(Optional.empty());

            // when
            final var result = service.findForCurrencyAndSpecificDate(currency, DateUtils.instantToLocalDateUTC(date));

            // then
            assertThat(result).isEmpty();
        }

    }

    @Nested
    class FindAllForCurrency {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindAllForCurrency_whenDatesWereNotPassed(final CurrencyCode currency) {
            // given
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(7));
            final Map<String, Object> details = Map.of();

            final var date_1 = DateUtils.localDateToInstant(LocalDate.now().minusDays(17));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRateToPLN(currency, 1.37);

            final var date_2 = DateUtils.localDateToInstant(LocalDate.now().minusDays(20));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRateToPLN(currency, 2.66);

            final var expected_1 = createNewExchangeRate(id_1, currency, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(repository.findByCurrency(eq(currency), any(Pageable.class))).thenReturn(new PageImpl<>(expectedList));

            final var result = service.findAllForCurrency(currency, null, null, PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency, expectedList);
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindAllForCurrency_whenDatesWerePassed(final CurrencyCode currency) {
            // given
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(11));
            final Map<String, Object> details = Map.of();

            final var date_1 = DateUtils.localDateToInstant(LocalDate.now().minusDays(15));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRateToPLN(currency, 3.27);

            final var date_2 = DateUtils.localDateToInstant(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRateToPLN(currency, 4.56);

            final var expected_1 = createNewExchangeRate(id_1, currency, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(repository.findByCurrencyAndDateBetween(eq(currency), eq(date_1), eq(date_2), any(Pageable.class))).thenReturn(new PageImpl<>(expectedList));

            final var result = service.findAllForCurrency(currency, DateUtils.instantToLocalDateUTC(date_1), DateUtils.instantToLocalDateUTC(date_2), PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency, expectedList);
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindAllForCurrency_whenDateFromWasNotPassed(final CurrencyCode currency) {
            // given
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(11));
            final Map<String, Object> details = Map.of();

            final var date_1 = DateUtils.localDateToInstant(LocalDate.now().minusDays(15));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRateToPLN(currency, 3.37);

            final var date_2 = DateUtils.localDateToInstant(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRateToPLN(currency, 4.86);

            final var expected_1 = createNewExchangeRate(id_1, currency, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(repository.findByCurrency(eq(currency), any(Pageable.class))).thenReturn(new PageImpl<>(expectedList));

            final var result = service.findAllForCurrency(currency, null, DateUtils.instantToLocalDateUTC(date_2), PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency, expectedList);
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindAllForCurrency_whenDateToWasNotPassed(final CurrencyCode currency) {
            // given
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(11));
            final Map<String, Object> details = Map.of();

            final var date_1 = DateUtils.localDateToInstant(LocalDate.now().minusDays(15));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRateToPLN(currency, 3.22);

            final var date_2 = DateUtils.localDateToInstant(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRateToPLN(currency, 4.53);

            final var expected_1 = createNewExchangeRate(id_1, currency, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(repository.findByCurrency(eq(currency), any(Pageable.class))).thenReturn(new PageImpl<>(expectedList));

            final var result = service.findAllForCurrency(currency, DateUtils.instantToLocalDateUTC(date_1), null, PageRequest.of(0, 1));

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
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(3));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.CAD;
            final var date_1 = DateUtils.localDateToInstant(LocalDate.now().minusDays(7));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRateToPLN(currency_1, 1.77);

            final var currency_2 = CurrencyCode.NZD;
            final var date_2 = DateUtils.localDateToInstant(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRateToPLN(currency_2, 2.14);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(repository.findByCurrency(eq(currency_1), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1)));
            when(repository.findByCurrency(eq(currency_2), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_2)));

            final var result = service.findAll(null, null, null, PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency_1, List.of(expected_1))
                    .forCurrencyHasExactlyTheSameAs(currency_2, List.of(expected_2));
        }

        @Test
        void shouldFindAll_whenDateFromWasNotPassed() {
            // given
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(3));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.GBP;
            final var date_1 = DateUtils.localDateToInstant(LocalDate.now().minusDays(7));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRateToPLN(currency_1, 1.2);

            final var currency_2 = CurrencyCode.JPY;
            final var date_2 = DateUtils.localDateToInstant(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRateToPLN(currency_2, 2.19);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(repository.findByCurrency(eq(currency_1), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1)));
            when(repository.findByCurrency(eq(currency_2), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_2)));

            final var result = service.findAll(null, null, DateUtils.instantToLocalDateUTC(date_1), PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency_1, List.of(expected_1))
                    .forCurrencyHasExactlyTheSameAs(currency_2, List.of(expected_2));
        }

        @Test
        void shouldFindAll_whenDateToWasNotPassed() {
            // given
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(3));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.CHF;
            final var date_1 = DateUtils.localDateToInstant(LocalDate.now().minusDays(7));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRateToPLN(currency_1, 2.95);

            final var currency_2 = CurrencyCode.AUD;
            final var date_2 = DateUtils.localDateToInstant(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRateToPLN(currency_2, 6.3);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, date_1, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, date_2, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(repository.findByCurrency(eq(currency_1), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1)));
            when(repository.findByCurrency(eq(currency_2), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_2)));

            final var result = service.findAll(null, DateUtils.instantToLocalDateUTC(date_2), null, PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency_1, List.of(expected_1))
                    .forCurrencyHasExactlyTheSameAs(currency_2, List.of(expected_2));
        }

        @Test
        void shouldFindAll_whenDateWasPassed() {
            // given
            final var date = DateUtils.localDateToInstant(LocalDate.now().minusDays(5));
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(3));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.CAD;
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRateToPLN(currency_1, 2.37);

            final var currency_2 = CurrencyCode.JPY;
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRateToPLN(currency_2, 1.84);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, date, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, date, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(repository.findByCurrencyAndDate(eq(currency_1), eq(date))).thenReturn(Optional.of(expected_1));
            when(repository.findByCurrencyAndDate(eq(currency_2), eq(date))).thenReturn(Optional.of(expected_2));

            final var result = service.findAll(DateUtils.instantToLocalDateUTC(date), null, null, PageRequest.of(0, 1));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency_1, List.of(expected_1))
                    .forCurrencyHasExactlyTheSameAs(currency_2, List.of(expected_2));
        }

        @Test
        void shouldFindAll_whenDatesRangeWasPassed() {
            // given
            final var date = DateUtils.localDateToInstant(LocalDate.now().minusDays(5));
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(3));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.SEK;
            final var date_1 = DateUtils.localDateToInstant(LocalDate.now().minusDays(7));
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRateToPLN(currency_1, 3.1);

            final var currency_2 = CurrencyCode.AUD;
            final var date_2 = DateUtils.localDateToInstant(LocalDate.now().minusDays(10));
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRateToPLN(currency_2, 6.66);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, date, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, date, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(repository.findByCurrencyAndDateBetween(eq(currency_1), eq(date_2), eq(date_1), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_1)));
            when(repository.findByCurrencyAndDateBetween(eq(currency_2), eq(date_2), eq(date_1), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expected_2)));

            final var result = service.findAll(null, DateUtils.instantToLocalDateUTC(date_2), DateUtils.instantToLocalDateUTC(date_1), PageRequest.of(0, 1));

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
            when(repository.saveAll(anyCollection())).thenReturn(List.of(currencyRatesSaved));

            final var resultCollection = service.createOrUpdate(List.of(currencyRateToSave));

            // then
            assertThat(resultCollection).isNotNull().hasSize(1);
            assertThat(resultCollection.stream().findAny()).isNotEmpty();
            final var result = resultCollection.stream().findAny().get();
            assertExchangeRate(result)
                    .hasId(ExchangeRateHelper.ID)
                    .isOfCurrency(currency)
                    .isOfDate(DateUtils.localDateToInstant(date))
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
            when(repository.findByCurrencyInAndDate(anySet(), any(Instant.class))).thenReturn(Stream.of(currencyRatesSaved));
            when(repository.saveAll(anyCollection())).thenReturn(List.of(currencyRatesSaved));

            final var resultCollection = service.createOrUpdate(List.of(currencyRateToSave));

            // then
            assertThat(resultCollection).isNotNull().hasSize(1);
            assertThat(resultCollection.stream().findAny()).isNotEmpty();
            final var result = resultCollection.stream().findAny().get();
            assertExchangeRate(result)
                    .hasId(ExchangeRateHelper.ID)
                    .isOfCurrency(currency)
                    .isOfDate(DateUtils.localDateToInstant(date))
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
            when(repository.findByCurrencyInAndDate(anySet(), any(Instant.class))).thenReturn(Stream.of(currencyRatesExistedWithoutProvider));
            when(repository.saveAll(anyCollection())).thenReturn(List.of(currencyRatesSaved));

            final var resultCollection = service.createOrUpdate(List.of(currencyRateToSave));

            // then
            assertThat(resultCollection).isNotNull().hasSize(1);
            assertThat(resultCollection.stream().findAny()).isNotEmpty();
            final var result = resultCollection.stream().findAny().get();
            assertExchangeRate(result)
                    .hasId(ExchangeRateHelper.ID)
                    .isOfCurrency(currency)
                    .isOfDate(DateUtils.localDateToInstant(date))
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
            when(repository.findByCurrencyInAndDateBetween(anySet(), any(Instant.class), any(Instant.class))).thenReturn(Stream.of(currencyRatesExistedWithoutProvider_1));
            when(repository.saveAll(anyCollection())).thenReturn(expectedResult);

            final var result = service.createOrUpdate(List.of(currencyRateToSave_1, currencyRateToSave_2, currencyRateToSave_3));

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedResult)
                    .forCurrencyHasExactlyTheSameAs(currency_1, List.of(currencyRatesSaved_1, currencyRatesSaved_3))
                    .forCurrencyHasExactlyTheSameAs(currency_2, List.of(currencyRatesSaved_2));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldThrowInvalidDateException_whenDatesAreIncorrect(final CurrencyCode currency) {
            // given
            final var rate = 5.8d;
            final var tableNumber = "test/table/number/02";

            final var currencyRateToSave = createCurrencyRate(currency, null, rate, TABLE_TYPE, tableNumber);

            // when && then
            assertThatThrownBy(() -> service.createOrUpdate(List.of(currencyRateToSave)))
                    .isInstanceOf(InvalidDateException.class)
                    .hasMessage("Invalid data, could not find date to.");
        }

    }

    @Nested
    class HistoryUpdate {

        @Test
        void historyUpdateIsCalled() {
            // given && when
            service.historyUpdate();

            // then
            verify(historyUpdate).update();
        }

    }

}