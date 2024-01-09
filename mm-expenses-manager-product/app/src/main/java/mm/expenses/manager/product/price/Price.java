package mm.expenses.manager.product.price;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.product.ProductCommonValidation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Price implements Serializable {

    public static final MathContext DECIMAL_DIGITS = MathContext.DECIMAL32;
    public static final RoundingMode ROUND_PRICE_VALUE_MODE = RoundingMode.HALF_EVEN;
    public static final int ROUND_PRICE_VALUE_DIGITS = 2;

    @JsonProperty("currency")
    private CurrencyCode currency;

    @JsonProperty("value")
    private BigDecimal value;

    public BigDecimal getValue() {
        return Objects.nonNull(value) ? withScale(value) : withScale(BigDecimal.ZERO);
    }

    public CurrencyCode getCurrency() {
        return Objects.nonNull(currency) ? currency : CurrencyCode.UNDEFINED;
    }

    @JsonIgnore
    public BigDecimal getOriginalValue() {
        return value;
    }

    @JsonIgnore
    public CurrencyCode getOriginalCurrency() {
        return currency;
    }

    @JsonIgnore
    public boolean isValueValid() {
        return ProductCommonValidation.isPriceValueValid(value);
    }

    @JsonIgnore
    public boolean isCurrencyCodeValid() {
        return ProductCommonValidation.isPriceCurrencyCodeValid(currency);
    }

    private BigDecimal withScale(final BigDecimal value) {
        return value.setScale(ROUND_PRICE_VALUE_DIGITS, ROUND_PRICE_VALUE_MODE);
    }

}
