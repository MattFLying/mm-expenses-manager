package mm.expenses.manager.product.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.val;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionRequest;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionResponse;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionValueDto;
import mm.expenses.manager.product.ProductApplicationSpringTest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static mm.expenses.manager.product.processor.ProductHelper.DEFAULT_CURRENCY;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class FinanceApiClientTest extends ProductApplicationSpringTest {

    @Value("${app.feign-client.finance.port}")
    private Integer financeApiPort;

    private WireMockServer wireMockServer;

    @Autowired
    public FinanceApiClient api;

    @Override
    protected void setupBeforeEachTest() {
        wireMockServer = new WireMockServer(financeApiPort);
        wireMockServer.start();

        configureFor("localhost", wireMockServer.port());
    }

    @Override
    protected void setupAfterEachTest() {
        wireMockServer.stop();
    }

    @ParameterizedTest
    @ArgumentsSource(CurrencyCodeArgument.class)
    void convertMultipleRates_shouldConvertSingleRate(final CurrencyCode currency) throws JsonProcessingException {
        // given
        val id = UUID.randomUUID().toString();
        val date = LocalDate.now();

        val requestedRateFrom = createRateFrom(currency, randomDouble());
        val requestedRateTo = createRequestRateTo(DEFAULT_CURRENCY);
        val request = createCurrencyConversionRequest(id, date.toString(), requestedRateFrom, requestedRateTo);
        val requestBody = new JSONArray().put(request);

        val responseRateTo = createResponseRateTo(DEFAULT_CURRENCY, randomDouble());
        val responseRateFrom = requestedRateFrom;
        val response = createCurrencyConversionResponse(id, date.toString(), requestedRateFrom, responseRateTo);
        val responseBody = new JSONArray().put(response);

        val requestedRateFromAsObject = objectMapper.readValue(responseRateFrom.toString(), CurrencyConversionValueDto.class);
        val requestBodyAsObjects = objectMapper.readValue(requestBody.toString(), new TypeReference<List<CurrencyConversionRequest>>() {
        });
        val responseRateToAsObject = objectMapper.readValue(responseRateTo.toString(), CurrencyConversionValueDto.class);
        val responseBodyAsObjects = objectMapper.readValue(responseBody.toString(), new TypeReference<List<CurrencyConversionResponse>>() {
        });

        stubFor(
                post("/calculations")
                        .withRequestBody(WireMock.equalToJson(requestBody.toString(), true, true))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBody(responseBody.toString())
                        )
        );

        // when
        val result = api.convertMultipleRates(requestBodyAsObjects);

        // then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .hasSameSizeAs(responseBodyAsObjects);

        val convertedCurrency = responseBodyAsObjects.get(0);
        assertThat(convertedCurrency).isNotNull()
                .isInstanceOf(CurrencyConversionResponse.class);

        assertThat(convertedCurrency.getId()).isEqualTo(id);
        assertThat(convertedCurrency.getDate()).isEqualTo(date);
        assertThat(convertedCurrency.getFrom()).isEqualTo(requestedRateFromAsObject);
        assertThat(convertedCurrency.getTo()).isEqualTo(responseRateToAsObject);
    }

    @ParameterizedTest
    @ArgumentsSource(CurrencyCodeArgument.class)
    void convertMultipleRates_shouldConvertMultipleRates(final CurrencyCode currency) throws JsonProcessingException {
        // given
        val id_1 = UUID.randomUUID().toString();
        val date_1 = LocalDate.now();
        val date_2 = LocalDate.now().minusDays(5);

        val requestedRateFrom_1 = createRateFrom(currency, randomDouble());
        val requestedRateTo_1 = createRequestRateTo(DEFAULT_CURRENCY);
        val request_1 = createCurrencyConversionRequest(id_1, date_1.toString(), requestedRateFrom_1, requestedRateTo_1);

        val requestedRateFrom_2 = createRateFrom(currency, randomDouble());
        val requestedRateTo_2 = createRequestRateTo(DEFAULT_CURRENCY);
        val request_2 = createCurrencyConversionRequest(null, date_2.toString(), requestedRateFrom_2, requestedRateTo_2);

        val requestBody = new JSONArray().put(request_1).put(request_2);

        val responseRateTo_1 = createResponseRateTo(DEFAULT_CURRENCY, randomDouble());
        val responseRateFrom_1 = requestedRateFrom_1;
        val response_1 = createCurrencyConversionResponse(id_1, date_1.toString(), requestedRateFrom_1, responseRateTo_1);

        val responseRateTo_2 = createResponseRateTo(DEFAULT_CURRENCY, randomDouble());
        val responseRateFrom_2 = requestedRateFrom_2;
        val response_2 = createCurrencyConversionResponse(null, date_2.toString(), requestedRateFrom_2, responseRateTo_2);

        val responseBody = new JSONArray().put(response_1).put(response_2);

        val requestedRateFromAsObject_1 = objectMapper.readValue(responseRateFrom_1.toString(), CurrencyConversionValueDto.class);
        val responseRateToAsObject_1 = objectMapper.readValue(responseRateTo_1.toString(), CurrencyConversionValueDto.class);

        val requestedRateFromAsObject_2 = objectMapper.readValue(responseRateFrom_2.toString(), CurrencyConversionValueDto.class);
        val responseRateToAsObject_2 = objectMapper.readValue(responseRateTo_2.toString(), CurrencyConversionValueDto.class);

        val requestBodyAsObjects = objectMapper.readValue(requestBody.toString(), new TypeReference<List<CurrencyConversionRequest>>() {
        });
        val responseBodyAsObjects = objectMapper.readValue(responseBody.toString(), new TypeReference<List<CurrencyConversionResponse>>() {
        });

        stubFor(
                post("/calculations")
                        .withRequestBody(WireMock.equalToJson(requestBody.toString(), true, true))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBody(responseBody.toString())
                        )
        );

        // when
        val result = api.convertMultipleRates(requestBodyAsObjects);

        // then
        assertThat(result).isNotNull()
                .isNotEmpty()
                .hasSameSizeAs(responseBodyAsObjects);

        val convertedCurrency_1 = responseBodyAsObjects.get(0);
        assertThat(convertedCurrency_1).isNotNull()
                .isInstanceOf(CurrencyConversionResponse.class);

        assertThat(convertedCurrency_1.getId()).isEqualTo(id_1);
        assertThat(convertedCurrency_1.getDate()).isEqualTo(date_1);
        assertThat(convertedCurrency_1.getFrom()).isEqualTo(requestedRateFromAsObject_1);
        assertThat(convertedCurrency_1.getTo()).isEqualTo(responseRateToAsObject_1);

        val convertedCurrency_2 = responseBodyAsObjects.get(1);
        assertThat(convertedCurrency_2).isNotNull()
                .isInstanceOf(CurrencyConversionResponse.class);

        assertThat(convertedCurrency_2.getId()).isNull();
        assertThat(convertedCurrency_2.getDate()).isEqualTo(date_2);
        assertThat(convertedCurrency_2.getFrom()).isEqualTo(requestedRateFromAsObject_2);
        assertThat(convertedCurrency_2.getTo()).isEqualTo(responseRateToAsObject_2);
    }

    private Double randomDouble() {
        return new Random().nextDouble();
    }

    private JSONObject createRateFrom(final CurrencyCode code, final Double value) {
        return createConversionBody(code, value);
    }

    private JSONObject createRequestRateTo(final CurrencyCode code) {
        return createConversionBody(code, null);
    }

    private JSONObject createResponseRateTo(final CurrencyCode code, final Double value) {
        return createConversionBody(code, value);
    }

    private JSONObject createCurrencyConversionRequest(final String id, final String date, final JSONObject from, final JSONObject to) {
        return createCurrencyConversionBody(id, date, from, to);
    }

    private JSONObject createCurrencyConversionResponse(final String id, final String date, final JSONObject from, final JSONObject to) {
        return createCurrencyConversionBody(id, date, from, to);
    }

    private JSONObject createConversionBody(final CurrencyCode code, final Double value) {
        try {
            val json = new JSONObject().put("code", code.getCode());
            if (Objects.nonNull(value)) {
                json.put("value", value);
            }
            return json;
        } catch (final JSONException exception) {
            throw new RuntimeException(exception);
        }
    }

    private JSONObject createCurrencyConversionBody(final String id, final String date, final JSONObject from, final JSONObject to) {
        try {
            return new JSONObject().put("id", id)
                    .put("date", date)
                    .put("from", from)
                    .put("to", to);
        } catch (final JSONException exception) {
            throw new RuntimeException(exception);
        }
    }

}