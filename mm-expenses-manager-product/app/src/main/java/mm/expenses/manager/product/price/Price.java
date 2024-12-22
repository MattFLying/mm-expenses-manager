package mm.expenses.manager.product.price;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.product.ProductCommonValidation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Price implements Serializable {

    @JsonProperty("currency")
    private CurrencyCode currency;

    @JsonProperty("value")
    private BigDecimal value;

    public BigDecimal getValue() {
        return BigDecimalWrapper.of(value);
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

}
