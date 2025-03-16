package mm.expenses.manager.finance.converter;

import lombok.val;
import mm.expenses.manager.common.mongodb.pagination.PaginationHelper;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.web.exception.ExceptionMessage;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionRequest;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionValueDto;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheService;
import mm.expenses.manager.finance.cache.exchangerate.latest.LatestCacheServiceTest;
import mm.expenses.manager.finance.converter.strategy.ConversionStrategyType;
import mm.expenses.manager.finance.currency.CurrenciesService;
import mm.expenses.manager.finance.exchangerate.ExchangeRateHelper;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static mm.expenses.manager.finance.exception.FinanceExceptionMessage.*;
import static mm.expenses.manager.finance.exchangerate.ExchangeRateHelper.createNewExchangeRate;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.PROVIDER_NAME;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CurrencyConverterControllerTest extends FinanceApplicationTest {

    private static final String BASE_URL = "/calculations";

    @MockBean
    private ExchangeRateService exchangeRateService;

    @MockBean
    private ExchangeRateCacheService exchangeRateCacheService;

    @MockBean
    private CurrenciesService currenciesService;

    @Autowired
    private LatestCacheServiceTest latestCacheTest;

    @Autowired
    private PaginationHelper pagination;

    @Autowired
    private CurrencyConverterService currencyConverterService;

    @Override
    protected void setupBeforeEachTest() {
        when(currenciesService.getCurrentCurrency()).thenReturn(DEFAULT_CURRENCY);
        when(currenciesService.getAvailableCurrencies()).thenCallRealMethod();
    }

    @Override
    protected void setupAfterEachTest() {
        reset(exchangeRateService);
        reset(exchangeRateCacheService);
        reset(currenciesService);
        latestCacheTest.reset();
    }

    @Nested
    class ConvertRate {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldConvertFromDifferentToDefault(final CurrencyCode currency) throws Exception {
            // given
            final var date = LocalDate.now().minusDays(2);
            final var createdModified = DateUtils.localDateToInstant(date.minusDays(5));
            final var dateAsInstant = DateUtils.localDateToInstant(date);
            final Map<String, Object> details = Map.of();

            final var id = UUID.randomUUID().toString();
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var expected = createNewExchangeRate(id, currency, dateAsInstant, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            latestCacheTest.saveInMemory(currency, expected);
            final var expectedConversionValue = convertForStrategy(ConversionStrategyType.TO_DEFAULT, null, rate.getTo().getValue());

            // when && then
            mockMvc.perform(get(fromDifferentToDefault(currency, 1)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date", is(date.toString())))
                    .andExpect(jsonPath("$.from.value", is(1.0)))
                    .andExpect(jsonPath("$.from.code", is(currency.toString())))
                    .andExpect(jsonPath("$.to.value", is(convert(expectedConversionValue))))
                    .andExpect(jsonPath("$.to.code", is(CurrencyCode.PLN.toString())));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldConvertFromDifferentToDefaultWithSpecificDate(final CurrencyCode currency) throws Exception {
            // given
            final var date = LocalDate.now().minusDays(2);
            final var createdModified = DateUtils.localDateToInstant(date.minusDays(5));
            final var dateAsInstant = DateUtils.localDateToInstant(date);
            final Map<String, Object> details = Map.of();

            final var id = UUID.randomUUID().toString();
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var expected = createNewExchangeRate(id, currency, dateAsInstant, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            final var expectedConversionValue = convertForStrategy(ConversionStrategyType.TO_DEFAULT, null, rate.getTo().getValue());

            // when
            when(exchangeRateCacheService.findForCurrencyAndSpecificDate(currency, date)).thenReturn(Optional.of(ExchangeRateCache.of(expected, true, PROVIDER_NAME)));

            // then
            mockMvc.perform(get(fromDifferentToDefaultWithDate(currency, 1, date)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date", is(date.toString())))
                    .andExpect(jsonPath("$.from.value", is(1.0)))
                    .andExpect(jsonPath("$.from.code", is(currency.toString())))
                    .andExpect(jsonPath("$.to.value", is(convert(expectedConversionValue))))
                    .andExpect(jsonPath("$.to.code", is(CurrencyCode.PLN.toString())));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldConvertFromDefaultToDifferent(final CurrencyCode currency) throws Exception {
            // given
            final var date = LocalDate.now().minusDays(2);
            final var createdModified = DateUtils.localDateToInstant(date.minusDays(5));
            final var dateAsInstant = DateUtils.localDateToInstant(date);
            final Map<String, Object> details = Map.of();

            final var id = UUID.randomUUID().toString();
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var expected = createNewExchangeRate(id, currency, dateAsInstant, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            latestCacheTest.saveInMemory(currency, expected);

            final var expectedConversionValue = convertForStrategy(ConversionStrategyType.FROM_DEFAULT, null, rate.getTo().getValue());

            // when && then
            mockMvc.perform(get(fromDefaultToDifferent(currency, 1)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date", is(date.toString())))
                    .andExpect(jsonPath("$.from.value", is(1.0)))
                    .andExpect(jsonPath("$.from.code", is(CurrencyCode.PLN.toString())))
                    .andExpect(jsonPath("$.to.value", is(convert(expectedConversionValue))))
                    .andExpect(jsonPath("$.to.code", is(currency.toString())));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldConvertFromDefaultToDifferentWithSpecificDate(final CurrencyCode currency) throws Exception {
            // given
            final var date = LocalDate.now().minusDays(2);
            final var createdModified = DateUtils.localDateToInstant(date.minusDays(5));
            final var dateAsInstant = DateUtils.localDateToInstant(date);
            final Map<String, Object> details = Map.of();

            final var id = UUID.randomUUID().toString();
            final var rate = ExchangeRateHelper.createNewRandomRateToPLN(currency);

            final var expected = createNewExchangeRate(id, currency, dateAsInstant, createdModified, Map.of(PROVIDER_NAME, rate), Map.of(PROVIDER_NAME, details));

            final var expectedConversionValue = convertForStrategy(ConversionStrategyType.FROM_DEFAULT, null, rate.getTo().getValue());

            // when
            when(exchangeRateService.findForCurrencyAndSpecificDate(currency, date)).thenReturn(Optional.of(expected));

            // then
            mockMvc.perform(get(fromDefaultToDifferentWithDate(currency, 1, date)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date", is(date.toString())))
                    .andExpect(jsonPath("$.from.value", is(1.0)))
                    .andExpect(jsonPath("$.from.code", is(CurrencyCode.PLN.toString())))
                    .andExpect(jsonPath("$.to.value", is(convert(expectedConversionValue))))
                    .andExpect(jsonPath("$.to.code", is(currency.toString())));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeCombinationsArgument.class)
        void shouldConvertFromDifferentToDifferent(final Pair<CurrencyCode, CurrencyCode> currencyPair) throws Exception {
            // given
            final var from = currencyPair.getLeft();
            final var to = currencyPair.getRight();

            final var date = LocalDate.now().minusDays(2);
            final var createdModified = DateUtils.localDateToInstant(date.minusDays(5));
            final var dateAsInstant = DateUtils.localDateToInstant(date);
            final Map<String, Object> details = Map.of();

            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(from);
            final var expected_1 = createNewExchangeRate(id_1, from, dateAsInstant, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));

            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(to);
            final var expected_2 = createNewExchangeRate(id_2, to, dateAsInstant, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            latestCacheTest.saveInMemory(from, expected_1);
            latestCacheTest.saveInMemory(to, expected_2);

            final var expectedConversionValue = convertForStrategy(ConversionStrategyType.DIFFERENT, rate_1.getTo().getValue(), rate_2.getTo().getValue());

            // when && then
            mockMvc.perform(get(fromDifferentToDifferent(from, to, 1)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date", is(date.toString())))
                    .andExpect(jsonPath("$.from.value", is(1.0)))
                    .andExpect(jsonPath("$.from.code", is(from.toString())))
                    .andExpect(jsonPath("$.to.value", is(convert(expectedConversionValue))))
                    .andExpect(jsonPath("$.to.code", is(to.toString())));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeCombinationsArgument.class)
        void shouldConvertFromDifferentToDifferentWithDateAndIdFromCache(final Pair<CurrencyCode, CurrencyCode> currencyPair) throws Exception {
            // given
            final var from = currencyPair.getLeft();
            final var to = currencyPair.getRight();

            final var date = LocalDate.now().minusDays(2);
            final var createdModified = DateUtils.localDateToInstant(date.minusDays(5));
            final var dateAsInstant = DateUtils.localDateToInstant(date);
            final Map<String, Object> details = Map.of();

            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(from);
            final var expected_1 = createNewExchangeRate(id_1, from, dateAsInstant, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));

            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(to);
            final var expected_2 = createNewExchangeRate(id_2, to, dateAsInstant, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedConversionValue = convertForStrategy(ConversionStrategyType.DIFFERENT, rate_1.getTo().getValue(), rate_2.getTo().getValue());
            final var idOfResult = UUID.randomUUID().toString();

            // when
            when(exchangeRateCacheService.findForCurrencyCodesAndSpecificDate(eq(Set.of(from, to)), eq(date))).thenReturn(List.of(
                    ExchangeRateCache.of(expected_1, true, PROVIDER_NAME), ExchangeRateCache.of(expected_2, true, PROVIDER_NAME)
            ));
            when(exchangeRateService.pageRequest(anyInt(), anyInt())).thenReturn(pagination.getPageRequest(0, CurrencyCode.values().length - 2));
            when(exchangeRateService.findForCurrencyCodesAndSpecificDate(eq(Set.of(from, to)), eq(date), any(Pageable.class))).thenReturn(Stream.of(new PageImpl<>(List.of(expected_1, expected_2))));

            // then
            mockMvc.perform(get(fromDifferentToDifferentWithDateAndId(from, to, 1, date, idOfResult)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date", is(date.toString())))
                    .andExpect(jsonPath("$.id", is(idOfResult)))
                    .andExpect(jsonPath("$.from.value", is(1.0)))
                    .andExpect(jsonPath("$.from.code", is(from.toString())))
                    .andExpect(jsonPath("$.to.value", is(convert(expectedConversionValue))))
                    .andExpect(jsonPath("$.to.code", is(to.toString())));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeCombinationsArgument.class)
        void shouldConvertFromDifferentToDifferentWithDateAndIdWhenCacheNoExist(final Pair<CurrencyCode, CurrencyCode> currencyPair) throws Exception {
            // given
            final var from = currencyPair.getLeft();
            final var to = currencyPair.getRight();

            final var date = LocalDate.now().minusDays(2);
            final var createdModified = DateUtils.localDateToInstant(date.minusDays(5));
            final var dateAsInstant = DateUtils.localDateToInstant(date);
            final Map<String, Object> details = Map.of();

            final var id_1 = UUID.randomUUID().toString();
            final var rate_1 = ExchangeRateHelper.createNewRandomRateToPLN(from);
            final var expected_1 = createNewExchangeRate(id_1, from, dateAsInstant, createdModified, Map.of(PROVIDER_NAME, rate_1), Map.of(PROVIDER_NAME, details));

            final var id_2 = UUID.randomUUID().toString();
            final var rate_2 = ExchangeRateHelper.createNewRandomRateToPLN(to);
            final var expected_2 = createNewExchangeRate(id_2, to, dateAsInstant, createdModified, Map.of(PROVIDER_NAME, rate_2), Map.of(PROVIDER_NAME, details));

            final var expectedConversionValue = convertForStrategy(ConversionStrategyType.DIFFERENT, rate_1.getTo().getValue(), rate_2.getTo().getValue());
            final var idOfResult = UUID.randomUUID().toString();

            // when
            when(exchangeRateService.pageRequest(anyInt(), anyInt())).thenReturn(pagination.getPageRequest(0, CurrencyCode.values().length - 2));
            when(exchangeRateService.findForCurrencyCodesAndSpecificDate(eq(Set.of(from, to)), eq(date), any(Pageable.class))).thenReturn(Stream.of(new PageImpl<>(List.of(expected_1, expected_2))));

            // then
            mockMvc.perform(get(fromDifferentToDifferentWithDateAndId(from, to, 1, date, idOfResult)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.date", is(date.toString())))
                    .andExpect(jsonPath("$.id", is(idOfResult)))
                    .andExpect(jsonPath("$.from.value", is(rate_1.getFrom().getValue().doubleValue())))
                    .andExpect(jsonPath("$.from.code", is(from.toString())))
                    .andExpect(jsonPath("$.to.value", is(convert(expectedConversionValue))))
                    .andExpect(jsonPath("$.to.code", is(to.toString())));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldReturnNotFound_whenGetFromDifferentToDefault_andValueIsZero(final CurrencyCode currency) throws Exception {
            mockMvc.perform(get(fromDifferentToDefault(currency, 0.0)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(CURRENCY_CONVERSION_VALUE_MUST_BE_GREATER_THAN_ZERO.getCode())))
                    .andExpect(jsonPath("$.message", is(CURRENCY_CONVERSION_VALUE_MUST_BE_GREATER_THAN_ZERO.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldReturnNotFound_whenGetFromDefaultToDifferent_andValueIsNegative(final CurrencyCode currency) throws Exception {
            mockMvc.perform(get(fromDefaultToDifferent(currency, -5)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(CURRENCY_CONVERSION_VALUE_MUST_BE_GREATER_THAN_ZERO.getCode())))
                    .andExpect(jsonPath("$.message", is(CURRENCY_CONVERSION_VALUE_MUST_BE_GREATER_THAN_ZERO.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenUnknownCurrencyWasPassedAsTo() throws Exception {
            mockMvc.perform(get(fromDefaultToDifferent(CurrencyCode.UNDEFINED, 1)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(CURRENCY_NOT_ALLOWED.getCode())))
                    .andExpect(jsonPath("$.message", is(CURRENCY_NOT_ALLOWED.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenUnknownCurrencyWasPassedAsFrom() throws Exception {
            mockMvc.perform(get(fromDifferentToDefault(CurrencyCode.UNDEFINED, 1)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(CURRENCY_NOT_ALLOWED.getCode())))
                    .andExpect(jsonPath("$.message", is(CURRENCY_NOT_ALLOWED.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

    }

    @Nested
    class ConvertMultipleRates {

        @Test
        void shouldReturnBadRequest_whenRequestBodyIsEmpty() throws Exception {
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content("[]"))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(CURRENCY_MULTIPLE_CONVERSION_NULL_REQUEST.getCode())))
                    .andExpect(jsonPath("$.message", is(CURRENCY_MULTIPLE_CONVERSION_NULL_REQUEST.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldReturnBadRequest_whenFromIsMissing(final CurrencyCode currency) throws Exception {
            // given
            val toDto = new CurrencyConversionValueDto();
            toDto.setCode(currency.getCode());

            val conversionDto = new CurrencyConversionRequest();
            conversionDto.setTo(toDto);

            val request = List.of(conversionDto);

            // when & then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(CURRENCY_MULTIPLE_CONVERSION_BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.message", is(CURRENCY_MULTIPLE_CONVERSION_BAD_REQUEST.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldReturnBadRequest_whenFromValueIsMissing(final CurrencyCode currency) throws Exception {
            // given
            val fromDto = new CurrencyConversionValueDto();
            fromDto.setCode(currency.getCode());

            val conversionDto = new CurrencyConversionRequest();
            conversionDto.setFrom(fromDto);

            val request = List.of(conversionDto);

            // when & then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(CURRENCY_MULTIPLE_CONVERSION_BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.message", is(CURRENCY_MULTIPLE_CONVERSION_BAD_REQUEST.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldReturnBadRequest_whenFromValueIsZero(final CurrencyCode currency) throws Exception {
            // given
            val fromDto = new CurrencyConversionValueDto();
            fromDto.setCode(currency.getCode());
            fromDto.setValue(0.0);

            val conversionDto = new CurrencyConversionRequest();
            conversionDto.setFrom(fromDto);

            val request = List.of(conversionDto);

            // when & then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(CURRENCY_MULTIPLE_CONVERSION_BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.message", is(CURRENCY_MULTIPLE_CONVERSION_BAD_REQUEST.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @Test
        void shouldReturnBadRequest_whenFromCodeIsNull() throws Exception {
            // given
            val fromDto = new CurrencyConversionValueDto();
            fromDto.setValue(1.0);

            val conversionDto = new CurrencyConversionRequest();
            conversionDto.setFrom(fromDto);

            val request = List.of(conversionDto);

            // when & then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isBadRequest())

                    .andExpect(jsonPath("$.code", is(CURRENCY_MULTIPLE_CONVERSION_BAD_REQUEST.getCode())))
                    .andExpect(jsonPath("$.message", is(CURRENCY_MULTIPLE_CONVERSION_BAD_REQUEST.getMessage())))
                    .andExpect(jsonPath("$.status", is(ExceptionMessage.formatStatus(HttpStatus.BAD_REQUEST))))
                    .andExpect(jsonPath("$.occurredAt", notNullValue()));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldReturnConvertedCurrency(final CurrencyCode currency) throws Exception {
            // given
            val date = LocalDate.now();

            val expected = createNewExchangeRate(currency, date);
            val rate = expected.getRateByProvider(PROVIDER_NAME);
            latestCacheTest.saveInMemory(currency, expected);

            val fromDto = new CurrencyConversionValueDto();
            fromDto.setCode(currency.getCode());
            fromDto.setValue(rate.getFrom().getValue().doubleValue());

            val toDto = new CurrencyConversionValueDto();
            toDto.setCode(DEFAULT_CURRENCY.getCode());

            val conversionDto = new CurrencyConversionRequest();
            conversionDto.setFrom(fromDto);
            conversionDto.setTo(toDto);
            conversionDto.setDate(date);
            conversionDto.setId(UUID.randomUUID().toString());

            val request = List.of(conversionDto);

            // when && then
            mockMvc.perform(post(BASE_URL).contentType(DATA_FORMAT_JSON).content(objectMapper.writeValueAsString(request)))
                    .andExpect(content().contentType(DATA_FORMAT_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$[0].id", is(conversionDto.getId())))
                    .andExpect(jsonPath("$[0].date", is(date.toString())))
                    .andExpect(jsonPath("$[0].from.value", is(fromDto.getValue())))
                    .andExpect(jsonPath("$[0].from.code", is(fromDto.getCode())))
                    .andExpect(jsonPath("$[0].to.value", is(rate.getTo().getValue().doubleValue())))
                    .andExpect(jsonPath("$[0].to.code", is(toDto.getCode())));
        }

    }

    private double convert(final BigDecimal value) {
        return BigDecimalWrapper.of(value).doubleValue();
    }

    private String fromDifferentToDefault(final CurrencyCode from, final double value) {
        return BASE_URL + "?from=" + from + "&to=" + DEFAULT_CURRENCY + "&value=" + value;
    }

    private String fromDifferentToDefaultWithDate(final CurrencyCode from, final int value, final LocalDate date) {
        return BASE_URL + "?from=" + from + "&to=" + DEFAULT_CURRENCY + "&value=" + value + "&date=" + date;
    }

    private String fromDefaultToDifferent(final CurrencyCode to, final int value) {
        return BASE_URL + "?from=" + DEFAULT_CURRENCY + "&to=" + to + "&value=" + value;
    }

    private String fromDefaultToDifferentWithDate(final CurrencyCode to, final int value, final LocalDate date) {
        return BASE_URL + "?from=" + DEFAULT_CURRENCY + "&to=" + to + "&value=" + value + "&date=" + date;
    }

    private String fromDifferentToDifferent(final CurrencyCode from, final CurrencyCode to, final int value) {
        return BASE_URL + "?from=" + from + "&to=" + to + "&value=" + value;
    }

    private String fromDifferentToDifferentWithDateAndId(final CurrencyCode from, final CurrencyCode to, final int value, final LocalDate date, final String id) {
        return BASE_URL + "?from=" + from + "&to=" + to + "&value=" + value + "&date=" + date + "&id=" + id;
    }

    private BigDecimal convertForStrategy(final ConversionStrategyType type, final BigDecimal from, final BigDecimal to) {
        return switch (type) {
            case TO_DEFAULT -> BigDecimalWrapper.of(to.multiply(BigDecimal.ONE, MathContext.DECIMAL32));
            case FROM_DEFAULT -> BigDecimalWrapper.of(BigDecimal.ONE.multiply(BigDecimal.ONE, MathContext.DECIMAL32).divide(to, MathContext.DECIMAL32));
            default -> BigDecimalWrapper.of(from.multiply(BigDecimal.ONE, MathContext.DECIMAL32).divide(to, MathContext.DECIMAL32));
        };
    }

}
