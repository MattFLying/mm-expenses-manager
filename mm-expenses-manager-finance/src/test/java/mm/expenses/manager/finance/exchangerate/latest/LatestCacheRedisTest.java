package mm.expenses.manager.finance.exchangerate.latest;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.pageable.PageHelper;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static mm.expenses.manager.finance.exchangerate.ExchangeRateAssert.assertExchangeRate;
import static mm.expenses.manager.finance.exchangerate.ExchangeRateHelper.createNewExchangeRate;
import static mm.expenses.manager.finance.exchangerate.ExchangeRatesAssert.assertExchangeRates;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.PROVIDER_NAME;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LatestCacheRedisTest extends FinanceApplicationTest {

    @MockBean
    private CurrencyRatesConfig currencyRatesConfig;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @MockBean
    private RedisTemplate<CurrencyCode, ExchangeRate> redisTemplate;

    @MockBean
    private ValueOperations<CurrencyCode, ExchangeRate> valueOperations;

    private LatestCacheRedis latestCacheMap;

    @Override
    protected void setupBeforeEachTest() {
        when(currencyRatesConfig.getDefaultCurrency()).thenReturn(DEFAULT_CURRENCY);
        when(currencyRatesConfig.getAllRequiredCurrenciesCode()).thenCallRealMethod();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(any(CurrencyCode.class), any(ExchangeRate.class));
        this.latestCacheMap = new LatestCacheRedis(currencyRatesConfig, exchangeRateService, redisTemplate);
    }

    @Override
    protected void setupAfterEachTest() {
        reset(currencyRatesConfig);
        reset(exchangeRateService);
        reset(redisTemplate);
        reset(valueOperations);
    }


    @Nested
    class SaveInMemory {

        @Test
        void shouldSaveInMemory() {
            // given
            final var today = DateUtils.localDateToInstant(LocalDate.now());
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(5));
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
            when(exchangeRateService.pageRequest(anyInt(), anyInt())).thenReturn(PageHelper.getPageRequest(0, 2));
            when(exchangeRateService.findByDate(any(Pageable.class), any(LocalDate.class))).thenReturn(Page.empty());
            when(exchangeRateService.findAll(eq(null), any(LocalDate.class), any(LocalDate.class), any(Pageable.class))).thenReturn(Stream.of(new PageImpl<>(expectedList)));
            when(valueOperations.get(eq(currency_1))).thenReturn(expected_1);
            when(valueOperations.get(eq(currency_2))).thenReturn(expected_2);
            latestCacheMap.saveInMemory();

            // then
            for (var result : latestCacheMap.getLatest()) {
                final var toCompare = result.getId().equals(id_1) ? expected_1 : expected_2;
                assertExchangeRate(result)
                        .hasId(toCompare.getId())
                        .isOfCurrency(toCompare.getCurrency())
                        .isOfDate(toCompare.getDate())
                        .createdAt(toCompare.getCreatedAt())
                        .modifiedAt(toCompare.getModifiedAt())
                        .hasRateForProvider(PROVIDER_NAME, toCompare.getRatesByProvider().get(PROVIDER_NAME))
                        .hasDetailsForProvider(PROVIDER_NAME, toCompare.getDetailsByProvider().get(PROVIDER_NAME))
                        .hasVersion(toCompare.getVersion());
            }

        }

    }

    @Nested
    class GetLatest {

        @Test
        void shouldGetAllLatest() {
            // given
            final var today = DateUtils.localDateToInstant(LocalDate.now());
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(5));
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
            when(exchangeRateService.pageRequest(anyInt(), anyInt())).thenReturn(PageHelper.getPageRequest(0, 2));
            when(exchangeRateService.findByDate(any(Pageable.class), any(LocalDate.class))).thenReturn(Page.empty());
            when(exchangeRateService.findAll(eq(null), any(LocalDate.class), any(LocalDate.class), any(Pageable.class))).thenReturn(Stream.of(new PageImpl<>(expectedList)));
            when(redisTemplate.opsForValue().multiGet(anySet())).thenReturn(expectedList);

            final var result = latestCacheMap.getLatest();

            // then
            assertExchangeRates(result)
                    .containsExactlyTheSameObjectsAs(expectedList)
                    .forCurrencyHasExactlyTheSameAs(currency_1, expected_1)
                    .forCurrencyHasExactlyTheSameAs(currency_2, expected_2);
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldGetLatestForCurrency(final CurrencyCode currency) {
            // given
            final var id = UUID.randomUUID().toString();
            final var today = DateUtils.localDateToInstant(LocalDate.now());
            final var createdModified = DateUtils.localDateToInstant(LocalDate.now().minusDays(5));
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);
            final Map<String, Object> details = Map.of();

            final var expected = createNewExchangeRate(id, currency, today, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            // when
            when(exchangeRateService.pageRequest(anyInt(), anyInt())).thenReturn(PageHelper.getPageRequest(0, 2));
            when(exchangeRateService.findByDate(any(Pageable.class), any(LocalDate.class))).thenReturn(new PageImpl<>(List.of(expected)));
            when(redisTemplate.opsForValue().get(currency)).thenReturn(expected);

            final var result = latestCacheMap.getLatest(currency);

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

    }

}