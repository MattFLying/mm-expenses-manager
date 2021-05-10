package mm.expenses.manager.finance.cache.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exchangerate.ExchangeRateHelper;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.*;

import static mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheAssert.assertExchangeRateCache;
import static mm.expenses.manager.finance.cache.exchangerate.ExchangeRatesCacheAssert.assertExchangeRatesCache;
import static mm.expenses.manager.finance.exchangerate.ExchangeRateHelper.createNewExchangeRate;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.PROVIDER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ExchangeRateCacheServiceTest extends FinanceApplicationTest {

    @MockBean
    private CurrencyProviders currencyProviders;

    @Autowired
    private ExchangeRateCacheService exchangeRateCacheService;


    @Override
    protected void setupBeforeEachTest() {
        when(currencyProviders.getProviderName()).thenReturn(PROVIDER_NAME);
    }

    @Override
    protected void setupAfterEachTest() {
        reset(currencyProviders);
        exchangeRateCacheService.clearCache();
    }


    @Nested
    class FindLatestForCurrency {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindLatestForCurrency(final CurrencyCode currency) {
            // given
            final var todayLocalDate = LocalDate.now();
            final var today = DateUtils.localDateToInstantUTC(todayLocalDate);
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var id = UUID.randomUUID().toString();
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var expected = createNewExchangeRate(id, currency, today, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            // when
            exchangeRateCacheService.saveFresh(List.of(expected));
            final var result = exchangeRateCacheService.findLatestForCurrency(currency);

            // then
            assertExchangeRateCache(result)
                    .hasId(expected.getId())
                    .isOfCurrency(expected.getCurrency())
                    .isOfDate(todayLocalDate)
                    .hasFrom(expected.getRateByProvider(PROVIDER_NAME))
                    .hasTo(expected.getRateByProvider(PROVIDER_NAME))
                    .isLatest();
        }

    }

    @Nested
    class FindAllLatestOfCurrencies {

        @Test
        void shouldFindAllLatestOfCurrencies() {
            // given
            final var todayLocalDate = LocalDate.now();
            final var today = DateUtils.localDateToInstantUTC(todayLocalDate);
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.USD;
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.AUD;
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_2);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, today, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, today, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            // when
            exchangeRateCacheService.saveFresh(List.of(expected_1, expected_2));
            final var result = exchangeRateCacheService.findAllLatestOfCurrencies(Set.of(currency_1, currency_2));

            // then
            assertExchangeRatesCache(result)
                    .forCurrencyHasExactlyTheSameAsAndIsLatest(currency_1, expected_1)
                    .forCurrencyHasExactlyTheSameAsAndIsLatest(currency_2, expected_2);
        }

    }

    @Nested
    class FindAllLatest {

        @Test
        void shouldFindAllLatest() {
            // given
            final var todayLocalDate = LocalDate.now();
            final var today = DateUtils.localDateToInstantUTC(todayLocalDate);
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.CHF;
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.EUR;
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_2);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, today, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, today, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            // when
            exchangeRateCacheService.saveFresh(List.of(expected_1, expected_2));
            final var result = exchangeRateCacheService.findAllLatest();

            // then
            assertExchangeRatesCache(result)
                    .forCurrencyHasExactlyTheSameAsAndIsLatest(currency_1, expected_1)
                    .forCurrencyHasExactlyTheSameAsAndIsLatest(currency_2, expected_2);
        }

    }

    @Nested
    class FindForCurrencyCodesAndSpecificDate {

        @Test
        void shouldFindForCurrencyCodesAndSpecificDate() {
            // given
            final var todayLocalDate = LocalDate.now();
            final var today = DateUtils.localDateToInstantUTC(todayLocalDate);
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.CAD;
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.EUR;
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_2);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, today, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, today, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            // when
            exchangeRateCacheService.saveFresh(List.of(expected_1, expected_2));
            final var result = exchangeRateCacheService.findForCurrencyCodesAndSpecificDate(Set.of(currency_1, currency_2), todayLocalDate);

            // then
            assertExchangeRatesCache(result)
                    .forCurrencyHasExactlyTheSameAsAndIsLatest(currency_1, expected_1)
                    .forCurrencyHasExactlyTheSameAsAndIsLatest(currency_2, expected_2);
        }

    }

    @Nested
    class FindForCurrencyAndSpecificDate {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldFindForCurrencyAndSpecificDate(final CurrencyCode currency) {
            // given
            final var todayLocalDate = LocalDate.now();
            final var today = DateUtils.localDateToInstantUTC(todayLocalDate);
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var id = UUID.randomUUID().toString();
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var expected = createNewExchangeRate(id, currency, today, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            // when
            exchangeRateCacheService.saveFresh(List.of(expected));
            final var result = exchangeRateCacheService.findForCurrencyAndSpecificDate(currency, todayLocalDate);

            // then
            assertExchangeRateCache(result)
                    .hasId(expected.getId())
                    .isOfCurrency(expected.getCurrency())
                    .isOfDate(todayLocalDate)
                    .hasFrom(expected.getRateByProvider(PROVIDER_NAME))
                    .hasTo(expected.getRateByProvider(PROVIDER_NAME))
                    .isLatest();
        }

    }

    @Nested
    class DisableLatest {

        @Test
        void shouldDisableLatest() {
            // given
            final var todayLocalDate = LocalDate.now();
            final var today = DateUtils.localDateToInstantUTC(todayLocalDate);
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.CAD;
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.EUR;
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_2);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, today, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, today, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedCache_1 = ExchangeRateCache.of(expected_1, true, PROVIDER_NAME);
            final var expectedCache_2 = ExchangeRateCache.of(expected_2, true, PROVIDER_NAME);

            // when
            exchangeRateCacheService.saveFresh(List.of(expected_1, expected_2));

            final var resultBefore = exchangeRateCacheService.findAllLatest();
            assertExchangeRatesCache(resultBefore)
                    .forCurrencyHasExactlyTheSameAsAndIsLatest(currency_1, expected_1)
                    .forCurrencyHasExactlyTheSameAsAndIsLatest(currency_2, expected_2);

            exchangeRateCacheService.disableLatest(List.of(expectedCache_1, expectedCache_2));

            final var result = exchangeRateCacheService.findAllLatest();

            // then
            assertExchangeRatesCache(result).isEmpty();
        }

    }

    @Nested
    class SaveFresh {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldSaveFresh(final CurrencyCode currency) {
            // given
            final var todayLocalDate = LocalDate.now();
            final var today = DateUtils.localDateToInstantUTC(todayLocalDate);
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var id = UUID.randomUUID().toString();
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var expected = createNewExchangeRate(id, currency, today, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            // when
            final var resultBefore = exchangeRateCacheService.findLatestForCurrency(currency);
            assertThat(resultBefore).isEmpty();

            exchangeRateCacheService.saveFresh(List.of(expected));

            final var result = exchangeRateCacheService.findLatestForCurrency(currency);

            // then
            assertExchangeRateCache(result)
                    .hasId(expected.getId())
                    .isOfCurrency(expected.getCurrency())
                    .isOfDate(todayLocalDate)
                    .hasFrom(expected.getRateByProvider(PROVIDER_NAME))
                    .hasTo(expected.getRateByProvider(PROVIDER_NAME))
                    .isLatest();
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldSaveFresh_withParameters(final CurrencyCode currency) {
            // given
            final var todayLocalDate = LocalDate.now();
            final var today = DateUtils.localDateToInstantUTC(todayLocalDate);
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var id = UUID.randomUUID().toString();
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var expected = createNewExchangeRate(id, currency, today, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            // when
            final var resultBefore = exchangeRateCacheService.findLatestForCurrency(currency);
            assertThat(resultBefore).isEmpty();

            exchangeRateCacheService.saveFresh(List.of(expected), true);

            final var result = exchangeRateCacheService.findLatestForCurrency(currency);

            // then
            assertExchangeRateCache(result)
                    .hasId(expected.getId())
                    .isOfCurrency(expected.getCurrency())
                    .isOfDate(todayLocalDate)
                    .hasFrom(expected.getRateByProvider(PROVIDER_NAME))
                    .hasTo(expected.getRateByProvider(PROVIDER_NAME))
                    .isLatest();
        }

    }

}