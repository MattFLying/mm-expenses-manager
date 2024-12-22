package mm.expenses.manager.common.utils.wrapper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Wrapper for {@link BigDecimal} values with common configuration to handle {@link BigDecimal} and {@link Double}
 * values to be used in whole code to keep the consistency.
 */
public class BigDecimalWrapper {

    /**
     * A MathContext object with a precision setting matching the IEEE 754R Decimal32 format, 7 digits, and a rounding mode of HALF_EVEN, the IEEE 754R default.
     */
    public static final MathContext DECIMAL_DIGITS = MathContext.DECIMAL32;

    public static final RoundingMode ROUND_CURRENCY_VALUE_MODE = RoundingMode.HALF_EVEN;
    public static final int ROUND_CURRENCY_VALUE_DIGITS = 2;

    public static BigDecimal of(Double value) {
        if (Objects.isNull(value)) {
            value = 0.00;
        }
        return BigDecimal.valueOf(value).setScale(ROUND_CURRENCY_VALUE_DIGITS, ROUND_CURRENCY_VALUE_MODE);
    }

    public static BigDecimal of(BigDecimal value) {
        if (Objects.isNull(value)) {
            value = BigDecimal.valueOf(0.00);
        }
        return value.setScale(ROUND_CURRENCY_VALUE_DIGITS, ROUND_CURRENCY_VALUE_MODE);
    }

    public static BigDecimal zero() {
        return BigDecimal.valueOf(0.00).setScale(ROUND_CURRENCY_VALUE_DIGITS, ROUND_CURRENCY_VALUE_MODE);
    }

    public static BigDecimal one() {
        return BigDecimal.valueOf(1.00).setScale(ROUND_CURRENCY_VALUE_DIGITS, ROUND_CURRENCY_VALUE_MODE);
    }

}
