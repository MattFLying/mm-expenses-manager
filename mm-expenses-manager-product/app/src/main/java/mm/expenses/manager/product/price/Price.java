package mm.expenses.manager.product.price;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.product.ProductCommonValidation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

@Data
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Price {

    public static final MathContext DECIMAL_DIGITS = MathContext.DECIMAL32;
    public static final RoundingMode ROUND_PRICE_VALUE_MODE = RoundingMode.HALF_EVEN;
    public static final int ROUND_PRICE_VALUE_DIGITS = 2;

    private final CurrencyCode currency;

    private final BigDecimal value;

    public BigDecimal getValue() {
        return Objects.nonNull(value) ? withScale(value) : withScale(BigDecimal.ZERO);
    }

    public BigDecimal getOriginalValue() {
        return value;
    }

    public CurrencyCode getCurrency() {
        return Objects.nonNull(currency) ? currency : CurrencyCode.UNDEFINED;
    }

    public CurrencyCode getOriginalCurrency() {
        return currency;
    }

    public boolean isValueValid() {
        return ProductCommonValidation.isPriceValueValid(value);
    }

    public boolean isCurrencyCodeValid() {
        return ProductCommonValidation.isPriceCurrencyCodeValid(currency);
    }

    private BigDecimal withScale(final BigDecimal value) {
        return value.setScale(ROUND_PRICE_VALUE_DIGITS, ROUND_PRICE_VALUE_MODE);
    }

}
