package mm.expenses.manager.common.utils.wrapper;

import lombok.val;
import mm.expenses.manager.common.utils.BaseInitTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BigDecimalWrapperTest extends BaseInitTest {

    @Test
    void ofDouble() throws Exception {
        // given
        double value = 1.25d;

        // when
        val of = BigDecimalWrapper.of(value);

        // then
        assertThat(of).isNotNull().isInstanceOf(BigDecimal.class);
        assertThat(of.doubleValue()).isEqualTo(value);
        assertThat(of).hasScaleOf(BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS);
    }

    @Test
    void ofDouble_nullValueShouldBeConvertedToZero() throws Exception {
        // given
        Double value = null;
        BigDecimal expectedValue = BigDecimal.valueOf(0.00).setScale(BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS, BigDecimalWrapper.ROUND_CURRENCY_VALUE_MODE);

        // when
        val of = BigDecimalWrapper.of(value);

        // then
        assertThat(of).isNotNull().isInstanceOf(BigDecimal.class);
        assertThat(of).isEqualTo(expectedValue);
        assertThat(of).hasScaleOf(BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS);
    }

    @Test
    void ofDouble_shouldRoundUp() throws Exception {
        // given
        double value = 1.515d;
        double expectedValue = 1.52d;

        // when
        val of = BigDecimalWrapper.of(value);

        // then
        assertThat(of).isNotNull().isInstanceOf(BigDecimal.class);
        assertThat(of.doubleValue()).isEqualTo(expectedValue);
        assertThat(of).hasScaleOf(BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS);
    }

    @Test
    void ofBigDecimal() throws Exception {
        // given
        BigDecimal value = BigDecimal.valueOf(1.27);

        // when
        val of = BigDecimalWrapper.of(value);

        // then
        assertThat(of).isNotNull().isInstanceOf(BigDecimal.class);
        assertThat(of).isEqualTo(value);
        assertThat(of).hasScaleOf(BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS);
    }

    @Test
    void ofBigDecimal_nullValueShouldBeConvertedToZero() throws Exception {
        // given
        BigDecimal value = null;
        BigDecimal expectedValue = BigDecimal.valueOf(0.00).setScale(BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS, BigDecimalWrapper.ROUND_CURRENCY_VALUE_MODE);

        // when
        val of = BigDecimalWrapper.of(value);

        // then
        assertThat(of).isNotNull().isInstanceOf(BigDecimal.class);
        assertThat(of).isEqualTo(expectedValue);
        assertThat(of).hasScaleOf(BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS);
    }

    @Test
    void ofBigDecimal_shouldRoundUp() throws Exception {
        // given
        BigDecimal value = BigDecimal.valueOf(3.2178);
        BigDecimal expectedValue = BigDecimal.valueOf(3.22);

        // when
        val of = BigDecimalWrapper.of(value);

        // then
        assertThat(of).isNotNull().isInstanceOf(BigDecimal.class);
        assertThat(of).isEqualTo(expectedValue);
        assertThat(of).hasScaleOf(BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS);
    }

    @Test
    void zero() throws Exception {
        // given
        BigDecimal expectedValue = BigDecimal.valueOf(0.00).setScale(BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS, BigDecimalWrapper.ROUND_CURRENCY_VALUE_MODE);

        // when
        val of = BigDecimalWrapper.zero();

        // then
        assertThat(of).isNotNull().isInstanceOf(BigDecimal.class);
        assertThat(of).isEqualTo(expectedValue);
        assertThat(of).hasScaleOf(BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS);
    }

    @Test
    void one() throws Exception {
        // given
        BigDecimal expectedValue = BigDecimal.valueOf(1.00).setScale(BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS, BigDecimalWrapper.ROUND_CURRENCY_VALUE_MODE);

        // when
        val of = BigDecimalWrapper.one();

        // then
        assertThat(of).isNotNull().isInstanceOf(BigDecimal.class);
        assertThat(of).isEqualTo(expectedValue);
        assertThat(of).hasScaleOf(BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS);
    }

}