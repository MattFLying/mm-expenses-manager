package mm.expenses.manager.common.utils.price;

import lombok.val;
import mm.expenses.manager.common.utils.BaseInitTest;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PriceTest {

    @Test
    void getValue_shouldReturnZeroValue_whenValueIsNull() {
        // given
        val price = new Price();

        // when
        val result = price.getValue();

        // then
        assertThat(result).isEqualTo(BigDecimalWrapper.zero());
    }

    @Test
    void getValue_shouldReturnZeroValue_whenValueIsZero() {
        // given
        val price = new Price(0.0, null);

        // when
        val result = price.getValue();

        // then
        assertThat(result).isEqualTo(BigDecimalWrapper.zero());
    }

    @Test
    void getValue_shouldReturnCorrectValue() {
        // given
        val value = 3.15d;
        val price = new Price(null, BigDecimal.valueOf(value));

        // when
        val result = price.getValue();

        // then
        assertThat(result).isEqualTo(BigDecimalWrapper.of(value));
    }

    @Test
    void getValue_shouldScaleReturnedValue() {
        // given
        val value = 2.616d;
        val expectedValue = 2.62d;
        val price = new Price(null, BigDecimal.valueOf(value));

        // when
        val result = price.getValue();

        // then
        assertThat(result).isEqualTo(BigDecimalWrapper.of(value));
        assertThat(result).isEqualTo(BigDecimalWrapper.of(expectedValue));
    }

    @Test
    void getCurrency_shouldReturnUndefinedCurrency_whenCurrencyIsNull() {
        // given
        val price = new Price();

        // when
        val result = price.getCurrency();

        // then
        assertThat(result).isEqualTo(CurrencyCode.UNDEFINED);
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void getCurrency_shouldReturnCorrectCurrency(final CurrencyCode currency) {
        // given
        val price = new Price(currency, null);

        // when
        val result = price.getCurrency();

        // then
        assertThat(result).isEqualTo(currency);
    }

    @Test
    void empty_shouldReturnPriceEqualToZeroAndUndefinedCurrency() {
        // given & when
        val result = Price.empty();

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.zero());
        assertThat(result.getCurrency()).isEqualTo(CurrencyCode.UNDEFINED);
        assertThat(result.getDate()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void empty_shouldReturnPriceEqualToZeroAndExpectedCurrency(final CurrencyCode currency) {
        // given & when
        val result = Price.empty(currency);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.zero());
        assertThat(result.getCurrency()).isEqualTo(currency);
        assertThat(result.getDate()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void multiply_shouldMultiplyValueByExpectedQuantity(final CurrencyCode currency) {
        // given
        val value = BigDecimal.valueOf(5.15);
        val quantity = 3.12d;
        val date = Instant.now();

        // when
        val result = Price.multiply(currency, value, quantity, date);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.of(value.multiply(BigDecimalWrapper.of(quantity))));
        assertThat(result.getCurrency()).isEqualTo(currency);
        assertThat(result.getDate()).isEqualTo(date);
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void multiply_shouldMultiplyValueByExpectedQuantity_whenDateIsNull(final CurrencyCode currency) {
        // given
        val value = BigDecimal.valueOf(5.18);
        val quantity = 3.12d;

        // when
        val result = Price.multiply(currency, value, quantity, null);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.of(value.multiply(BigDecimalWrapper.of(quantity))));
        assertThat(result.getCurrency()).isEqualTo(currency);
        assertThat(result.getDate()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void multiply_shouldReturnEmpty_whenValueIsNull(final CurrencyCode currency) {
        // given
        val quantity = 3.12d;

        // when
        val result = Price.multiply(currency, null, quantity, null);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.zero());
        assertThat(result.getCurrency()).isEqualTo(CurrencyCode.UNDEFINED);
        assertThat(result.getDate()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void multiply_shouldReturnEmpty_whenQuantityIsNull(final CurrencyCode currency) {
        // given
        val value = BigDecimal.valueOf(4.21);

        // when
        val result = Price.multiply(currency, value, null, null);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.zero());
        assertThat(result.getCurrency()).isEqualTo(CurrencyCode.UNDEFINED);
        assertThat(result.getDate()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void multiply_shouldReturnEmpty_whenValueAndQuantityAreNull(final CurrencyCode currency) {
        // given & when
        val result = Price.multiply(currency, null, null, null);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.zero());
        assertThat(result.getCurrency()).isEqualTo(CurrencyCode.UNDEFINED);
        assertThat(result.getDate()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void multiply_shouldMultiplyPriceByExpectedQuantity(final CurrencyCode currency) {
        // given
        val value = BigDecimal.valueOf(5.18);
        val quantity = 3.12d;
        val price = new Price(currency, value);

        // when
        val result = Price.multiply(price, quantity);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.of(value.multiply(BigDecimalWrapper.of(quantity))));
        assertThat(result.getCurrency()).isEqualTo(currency);
        assertThat(result.getDate()).isNull();
    }

    @Test
    void multiply_shouldReturnEmpty_whenQuantityIsCorrectAndPriceIsNull() {
        // given
        val quantity = 3.12d;

        // when
        val result = Price.multiply(null, quantity);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.zero());
        assertThat(result.getCurrency()).isEqualTo(CurrencyCode.UNDEFINED);
        assertThat(result.getDate()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void multiply_shouldReturnEmpty_whenPriceIsCorrectAndQuantityIsNull(final CurrencyCode currency) {
        // given
        val value = BigDecimal.valueOf(5.18);
        val price = new Price(currency, value);

        // when
        val result = Price.multiply(price, null);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.zero());
        assertThat(result.getCurrency()).isEqualTo(CurrencyCode.UNDEFINED);
        assertThat(result.getDate()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void multiply_shouldReturnEmpty_whenPriceAndQuantityAreNull(final CurrencyCode currency) {
        // given & when
        val result = Price.multiply(null, null);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.zero());
        assertThat(result.getCurrency()).isEqualTo(CurrencyCode.UNDEFINED);
        assertThat(result.getDate()).isNull();
    }

    @Test
    void add_shouldReturnEmpty_whenBothPricesAreNull() {
        // given & when
        val result = Price.add(null, null);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.zero());
        assertThat(result.getCurrency()).isEqualTo(CurrencyCode.UNDEFINED);
        assertThat(result.getDate()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void add_shouldReturnEmpty_whenFirstPriceIsNull(final CurrencyCode currency) {
        // given
        val value = BigDecimal.valueOf(5.18);
        val second = new Price(currency, value);

        // when
        val result = Price.add(null, second);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.zero());
        assertThat(result.getCurrency()).isEqualTo(CurrencyCode.UNDEFINED);
        assertThat(result.getDate()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void add_shouldReturnEmpty_whenSecondPriceIsNull(final CurrencyCode currency) {
        // given
        val value = BigDecimal.valueOf(5.18);
        val first = new Price(currency, value);

        // when
        val result = Price.add(first, null);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.zero());
        assertThat(result.getCurrency()).isEqualTo(CurrencyCode.UNDEFINED);
        assertThat(result.getDate()).isNull();
    }

    @ParameterizedTest
    @ArgumentsSource(BaseInitTest.CurrencyCodeArgument.class)
    void add_shouldAdd(final CurrencyCode currency) {
        // given
        val firstValue = BigDecimal.valueOf(1.12);
        val secondValue = BigDecimal.valueOf(3.33);

        val first = new Price(currency, firstValue);
        val second = new Price(currency, secondValue);

        // when
        val result = Price.add(first, second);

        // then
        assertThat(result.getValue()).isEqualTo(BigDecimalWrapper.of(firstValue.add(secondValue)));
        assertThat(result.getCurrency()).isEqualTo(currency);
        assertThat(result.getDate()).isNotNull();
    }

}