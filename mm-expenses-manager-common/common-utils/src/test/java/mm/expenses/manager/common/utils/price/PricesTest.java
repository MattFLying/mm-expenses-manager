package mm.expenses.manager.common.utils.price;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import mm.expenses.manager.common.utils.BaseInitTest;
import mm.expenses.manager.common.utils.exception.PriceIllegalArgumentException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricesTest {

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void shouldNotCreatePrices_whenDuplicatedCurrenciesPassed(final CurrencyCode currency) {
        // given
        val value_1 = BigDecimal.valueOf(1.33);
        val value_2 = BigDecimal.valueOf(2.21);
        val date = Instant.now();
        val price_1 = new Price(currency, value_1, date);
        val price_2 = new Price(currency, value_2, date);
        val pricesList = List.of(price_1, price_2);

        // when & then
        assertThatThrownBy(() -> new Prices(pricesList))
                .isInstanceOf(PriceIllegalArgumentException.class)
                .hasMessage(PriceIllegalArgumentException.ERROR_MESSAGE);
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void containsCurrency_shouldReturnTrue_whenCurrencyExists(final CurrencyCode currency) {
        // given
        val value = 1.746d;
        val price = new Price(currency, BigDecimal.valueOf(value));
        val prices = new Prices(price);

        // when
        val result = prices.containsCurrency(currency);

        // then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void containsCurrency_shouldReturnFalse_whenCurrencyDoesNotExists(final CurrencyCode currency) {
        // given
        val prices = new Prices();

        // when
        val result = prices.containsCurrency(currency);

        // then
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void containsCurrency_shouldReturnTrue_whenCurrencyAsStringExists(final CurrencyCode currency) {
        // given
        val value = 2.986d;
        val price = new Price(currency, BigDecimal.valueOf(value));
        val prices = new Prices(price);

        // when
        val result = prices.containsCurrency(currency.getCode());

        // then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void containsCurrency_shouldReturnFalse_whenCurrencyAsStringDoesNotExists(final CurrencyCode currency) {
        // given
        val prices = new Prices();

        // when
        val result = prices.containsCurrency(currency.getCode());

        // then
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void multiply_shouldMultiplyPricesByExpectedQuantity(final CurrencyCode currency) {
        // given
        val value = BigDecimal.valueOf(2.15);
        val quantity = 4.33d;
        val date = Instant.now();
        val price = new Price(currency, value, date);
        val prices = new Prices(price);

        // when
        val result = Prices.multiply(prices, quantity);

        // then
        assertThat(result.isEmpty()).isFalse();
        assertThat(result).hasSameSizeAs(prices);

        val resultedPrice = result.get(0);
        assertThat(resultedPrice.getValue()).isEqualTo(BigDecimalWrapper.of(value.multiply(BigDecimalWrapper.of(quantity))));
        assertThat(resultedPrice.getCurrency()).isEqualTo(currency);
        assertThat(resultedPrice.getDate()).isEqualTo(date);
    }

    @Test
    void multiply_shouldReturnEmptyPrices_whenPricesToMultiplyAreNull() {
        // given
        val quantity = 7.12d;

        // when
        val result = Prices.multiply(null, quantity);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void multiply_shouldReturnEmptyPrices_whenQuantityIsNull(final CurrencyCode currency) {
        // given
        val value = BigDecimal.valueOf(3.12);
        val date = Instant.now();
        val price = new Price(currency, value, date);
        val prices = new Prices(price);

        // when
        val result = Prices.multiply(prices, null);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void multiply_shouldReturnEmptyPrices_whenQuantityAndPricesAreNull() {
        // given & when
        val result = Prices.multiply(null, null);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void of_shouldCreatePricesFromPricesList(final CurrencyCode currency) {
        // given
        val value_1 = BigDecimal.valueOf(8.11);
        val value_2 = BigDecimal.valueOf(2.22);
        val date = Instant.now();
        val price_1 = new Price(currency, value_1, date);
        val price_2 = new Price(generateDifferentThan(currency), value_2, date);
        val pricesList = List.of(price_1, price_2);

        // when
        val result = new Prices(pricesList);

        // then
        assertThat(result.isEmpty()).isFalse();
        assertThat(result).hasSameSizeAs(pricesList);

        val resultedPrice_1 = result.get(0);
        assertThat(resultedPrice_1.getValue()).isEqualTo(price_1.getValue());
        assertThat(resultedPrice_1.getCurrency()).isEqualTo(price_1.getCurrency());
        assertThat(resultedPrice_1.getDate()).isEqualTo(price_1.getDate());

        val resultedPrice_2 = result.get(1);
        assertThat(resultedPrice_2.getValue()).isEqualTo(price_2.getValue());
        assertThat(resultedPrice_2.getCurrency()).isEqualTo(price_2.getCurrency());
        assertThat(resultedPrice_2.getDate()).isEqualTo(price_2.getDate());
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void of_shouldCreatePricesFromPricesList_mergeTwoSameCurrencies(final CurrencyCode currency) {
        // given
        val value_1 = BigDecimal.valueOf(1.33);
        val value_2 = BigDecimal.valueOf(2.21);
        val date = Instant.now();
        val price_1 = new Price(currency, value_1, date);
        val price_2 = new Price(currency, value_2, date);
        val pricesList = List.of(price_1, price_2);

        // when
        val result = Prices.of(pricesList);

        // then
        assertThat(result.isEmpty()).isFalse();
        assertThat(result).hasSize(1);

        val resultedPrice = result.get(0);
        assertThat(resultedPrice.getValue()).isEqualTo(BigDecimalWrapper.of(value_1.add(value_2)));
        assertThat(resultedPrice.getCurrency()).isEqualTo(price_1.getCurrency());
        assertThat(resultedPrice.getDate()).isEqualTo(price_1.getDate()).isEqualTo(price_1.getDate());
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void calculatePriceSummary_shouldCorrectlyCalculatePricesSummary(final CurrencyCode currency) {
        // given
        val date = Instant.now();
        val quantity = 2.45d;

        val value_1 = BigDecimal.valueOf(3.21);
        val price_1 = new Price(currency, value_1, date);
        val prices_1 = Prices.of(List.of(price_1));
        val summary_1 = new TestPriceSummary(prices_1, quantity);

        val value_2 = BigDecimal.valueOf(6.43);
        val price_2 = new Price(generateDifferentThan(currency), value_2, date);
        val prices_2 = Prices.of(List.of(price_2));
        val summary_2 = new TestPriceSummary(prices_2, quantity);

        val summaryPrices = List.of(summary_1, summary_2);

        // when
        val result = Prices.calculatePriceSummary(summaryPrices);

        // then
        assertThat(result.isEmpty()).isFalse();
        assertThat(result).hasSameSizeAs(summaryPrices);

        val resultedPrice_1 = result.get(0);
        assertThat(resultedPrice_1.getValue()).isEqualTo(BigDecimalWrapper.of(Price.multiply(price_1, quantity).getValue()));
        assertThat(resultedPrice_1.getCurrency()).isEqualTo(price_1.getCurrency());
        assertThat(resultedPrice_1.getDate()).isEqualTo(price_1.getDate()).isEqualTo(price_1.getDate());

        val resultedPrice_2 = result.get(1);
        assertThat(resultedPrice_2.getValue()).isEqualTo(BigDecimalWrapper.of(Price.multiply(price_2, quantity).getValue()));
        assertThat(resultedPrice_2.getCurrency()).isEqualTo(price_2.getCurrency());
        assertThat(resultedPrice_2.getDate()).isEqualTo(price_2.getDate()).isEqualTo(price_2.getDate());
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void calculatePriceSummary_shouldCorrectlyCalculatePricesSummary2(final CurrencyCode currency) {
        // given
        val date = Instant.now();
        val quantity = 2.45d;

        val value_1 = BigDecimal.valueOf(7.32);
        val price_1 = new Price(currency, value_1, date);
        val prices_1 = Prices.of(List.of(price_1));
        val summary_1 = new TestPriceSummary(prices_1, quantity);
        val expectedValue_1 = Price.multiply(price_1, quantity).getValue();

        val value_2 = BigDecimal.valueOf(1.21);
        val price_2 = new Price(currency, value_2, date);
        val prices_2 = Prices.of(List.of(price_2));
        val summary_2 = new TestPriceSummary(prices_2, quantity);
        val expectedValue_2 = Price.multiply(price_2, quantity).getValue();

        val summaryPrices = List.of(summary_1, summary_2);

        // when
        val result = Prices.calculatePriceSummary(summaryPrices);

        // then
        assertThat(result.isEmpty()).isFalse();
        assertThat(result).hasSize(1);

        val resultedPrice_1 = result.get(0);
        assertThat(resultedPrice_1.getValue()).isEqualTo(BigDecimalWrapper.of(expectedValue_1.add(expectedValue_2)));
        assertThat(resultedPrice_1.getCurrency()).isEqualTo(price_1.getCurrency());
        assertThat(resultedPrice_1.getDate()).isEqualTo(price_1.getDate()).isEqualTo(price_1.getDate());
    }

    @Test
    void calculatePriceSummary_shouldReturnEmptyPrices_whenObjectsToCalculateAreNull() {
        // given & when
        val result = Prices.calculatePriceSummary(null);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void calculatePriceSummary_shouldReturnEmptyPrices_whenObjectsToCalculateAreEmpty() {
        // given & when
        val result = Prices.calculatePriceSummary(List.of());

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    private CurrencyCode generateDifferentThan(final CurrencyCode currencyCode) {
        val listWithoutGivenCurrency = Stream.of(CurrencyCode.values()).filter(currency -> !currency.equals(currencyCode)).toList();
        return listWithoutGivenCurrency.stream()
                .skip(new Random().nextInt(listWithoutGivenCurrency.size()))
                .findFirst()
                .orElse(CurrencyCode.UNDEFINED);
    }

    @Data
    @AllArgsConstructor
    private static class TestPriceSummary implements PriceSummary {

        private Prices price;
        private Double quantity;

        @Override
        public Prices getPriceSummary() {
            return Objects.nonNull(price) ? Prices.multiply(price, quantity) : new Prices();
        }

    }

}