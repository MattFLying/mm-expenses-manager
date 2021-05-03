package mm.expenses.manager.finance.cache.exchangerate.latest;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.pageable.PageHelper;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheService;
import mm.expenses.manager.finance.exchangerate.ExchangeRateHelper;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheAssert.assertExchangeRateCache;
import static mm.expenses.manager.finance.cache.exchangerate.ExchangeRatesCacheAssert.assertExchangeRatesCache;
import static mm.expenses.manager.finance.exchangerate.ExchangeRateHelper.createNewExchangeRate;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.PROVIDER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LatestCacheMapTest extends FinanceApplicationTest {

    @MockBean
    private CurrencyRatesConfig currencyRatesConfig;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @MockBean
    private ExchangeRateCacheService exchangeRateCacheService;

    private LatestCacheMap latestCacheMap;

    @Override
    protected void setupBeforeEachTest() {
        when(currencyRatesConfig.getDefaultCurrency()).thenReturn(DEFAULT_CURRENCY);
        when(currencyRatesConfig.getAllRequiredCurrenciesCode()).thenCallRealMethod();
        this.latestCacheMap = new LatestCacheMap(currencyRatesConfig, exchangeRateService, exchangeRateCacheService);
    }

    @Override
    protected void setupAfterEachTest() {
        reset(currencyRatesConfig);
        reset(exchangeRateService);
        reset(exchangeRateCacheService);
    }


    @Test
    void shouldRetrieveRedisCacheType() {
        assertThat(latestCacheMap.cacheType()).isEqualTo(LatestCacheInit.CacheType.MAP);
    }

    @Nested
    class SaveInMemory {

        @Test
        void shouldSaveInMemory_whenNoCacheExist() {
            // given
            final var todayLocalDate = LocalDate.now();
            final var today = DateUtils.localDateToInstantUTC(todayLocalDate);
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.AUD;
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.NZD;
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, today, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, today, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateService.pageRequest(anyInt(), anyInt())).thenReturn(PageHelper.getPageRequest(0, 2));
            when(exchangeRateService.findByDate(any(Pageable.class), any(LocalDate.class))).thenReturn(Page.empty());
            when(exchangeRateService.findAll(eq(null), any(LocalDate.class), any(LocalDate.class), any(Pageable.class))).thenReturn(Stream.of(new PageImpl<>(expectedList)));
            when(exchangeRateCacheService.findAllLatest()).thenReturn(Collections.emptyList());

            latestCacheMap.saveInMemory();

            // then
            for (var result : latestCacheMap.getLatest()) {
                final var toCompare = result.getId().equals(id_1) ? expected_1 : expected_2;
                assertExchangeRateCache(result)
                        .hasId(toCompare.getId())
                        .isOfCurrency(toCompare.getCurrency())
                        .isOfDate(todayLocalDate)
                        .hasFrom(toCompare.getRateByProvider(PROVIDER_NAME))
                        .hasTo(toCompare.getRateByProvider(PROVIDER_NAME))
                        .isNotLatest();
            }
            verify(exchangeRateCacheService, times(0)).disableLatest(anyList());
            verify(exchangeRateCacheService).saveFresh(any());
        }

        @Test
        void shouldSaveInMemory_whenCacheIsUpToDateThenSkip() {
            // given
            final var todayLocalDate = LocalDate.now();
            final var today = DateUtils.localDateToInstantUTC(todayLocalDate);
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.CHF;
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.GBP;
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, today, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, today, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateService.pageRequest(anyInt(), anyInt())).thenReturn(PageHelper.getPageRequest(0, 2));
            when(exchangeRateService.findByDate(any(Pageable.class), any(LocalDate.class))).thenReturn(Page.empty());
            when(exchangeRateService.findAll(eq(null), any(LocalDate.class), any(LocalDate.class), any(Pageable.class))).thenReturn(Stream.of(new PageImpl<>(expectedList)));
            when(exchangeRateCacheService.findAllLatest()).thenReturn(List.of(ExchangeRateCache.of(expected_1, true, PROVIDER_NAME), ExchangeRateCache.of(expected_2, true, PROVIDER_NAME)));

            latestCacheMap.saveInMemory();

            // then
            for (var result : latestCacheMap.getLatest()) {
                final var toCompare = result.getId().equals(id_1) ? expected_1 : expected_2;
                assertExchangeRateCache(result)
                        .hasId(toCompare.getId())
                        .isOfCurrency(toCompare.getCurrency())
                        .isOfDate(todayLocalDate)
                        .hasFrom(toCompare.getRateByProvider(PROVIDER_NAME))
                        .hasTo(toCompare.getRateByProvider(PROVIDER_NAME))
                        .isLatest();
            }
            verify(exchangeRateCacheService, times(0)).disableLatest(anyList());
            verify(exchangeRateCacheService, times(0)).saveFresh(anyList());
        }

        @Test
        void shouldSaveInMemory_whenCacheHasRateThenDisableLatestAndSaveFresh() {
            // given
            final var todayLocalDate = LocalDate.now();
            final var today = DateUtils.localDateToInstantUTC(todayLocalDate);
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final Map<String, Object> details = Map.of();

            final var currency_1 = CurrencyCode.USD;
            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var currency_2 = CurrencyCode.JPY;
            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(currency_1);

            final var expected_1 = createNewExchangeRate(id_1, currency_1, today, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));
            final var expected_2 = createNewExchangeRate(id_2, currency_2, today, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedList = List.of(expected_1, expected_2);

            // when
            when(exchangeRateService.pageRequest(anyInt(), anyInt())).thenReturn(PageHelper.getPageRequest(0, 2));
            when(exchangeRateService.findByDate(any(Pageable.class), any(LocalDate.class))).thenReturn(Page.empty());
            when(exchangeRateService.findAll(eq(null), any(LocalDate.class), any(LocalDate.class), any(Pageable.class))).thenReturn(Stream.of(new PageImpl<>(expectedList)));
            when(exchangeRateCacheService.findAllLatest()).thenReturn(List.of(ExchangeRateCache.of(expected_1, true, PROVIDER_NAME)));

            latestCacheMap.saveInMemory();

            // then
            for (var result : latestCacheMap.getLatest()) {
                final var toCompare = result.getId().equals(id_1) ? expected_1 : expected_2;
                assertExchangeRateCache(result)
                        .hasId(toCompare.getId())
                        .isOfCurrency(toCompare.getCurrency())
                        .isOfDate(todayLocalDate)
                        .hasFrom(toCompare.getRateByProvider(PROVIDER_NAME))
                        .hasTo(toCompare.getRateByProvider(PROVIDER_NAME))
                        .isLatest();
            }
            verify(exchangeRateCacheService).disableLatest(any());
            verify(exchangeRateCacheService).saveFresh(any());
        }

    }

    @Nested
    class GetLatest {

        @Test
        void shouldGetAllLatest() {
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

            // when
            when(exchangeRateCacheService.findAllLatest()).thenReturn(List.of(
                    ExchangeRateCache.of(expected_1, true, PROVIDER_NAME), ExchangeRateCache.of(expected_2, true, PROVIDER_NAME)
            ));

            final var result = latestCacheMap.getLatest();

            // then
            assertExchangeRatesCache(result)
                    .forCurrencyHasExactlyTheSameAsAndIsLatest(currency_1, expected_1)
                    .forCurrencyHasExactlyTheSameAsAndIsLatest(currency_2, expected_2);
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldGetLatestForCurrency(final CurrencyCode currency) {
            // given
            final var todayLocalDate = LocalDate.now();
            final var id = UUID.randomUUID().toString();
            final var today = DateUtils.localDateToInstantUTC(todayLocalDate);
            final var createdModified = DateUtils.localDateToInstantUTC(LocalDate.now().minusDays(5));
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);
            final Map<String, Object> details = Map.of();

            final var expected = createNewExchangeRate(id, currency, today, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            // when
            when(exchangeRateCacheService.findLatestForCurrency(eq(currency))).thenReturn(Optional.of(ExchangeRateCache.of(expected, true, PROVIDER_NAME)));

            final var result = latestCacheMap.getLatest(currency);

            // then
            assertExchangeRateCache(result)
                    .hasId(id)
                    .isOfCurrency(currency)
                    .isOfDate(todayLocalDate)
                    .hasFrom(expected.getRateByProvider(PROVIDER_NAME))
                    .hasTo(expected.getRateByProvider(PROVIDER_NAME))
                    .isLatest();
        }

        @Test
        void shouldGetLatest() {
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

            // when
            when(exchangeRateCacheService.findAllLatestOfCurrencies(anySet())).thenReturn(List.of(
                    ExchangeRateCache.of(expected_1, true, PROVIDER_NAME), ExchangeRateCache.of(expected_2, true, PROVIDER_NAME)
            ));

            final var result = latestCacheMap.getLatest(Set.of(currency_1, currency_2));

            // then
            assertThat(result).hasSize(2);
            assertExchangeRatesCache(result.values())
                    .forCurrencyHasExactlyTheSameAsAndIsLatest(currency_1, expected_1)
                    .forCurrencyHasExactlyTheSameAsAndIsLatest(currency_2, expected_2);
        }

    }

}

