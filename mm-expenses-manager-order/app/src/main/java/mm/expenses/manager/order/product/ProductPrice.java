package mm.expenses.manager.order.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ProductPrice implements Serializable {

    @JsonProperty("currency")
    private CurrencyCode currency;

    @JsonProperty("value")
    private BigDecimal value;

    @JsonProperty("date")
    private String date;

    @JsonProperty("isOriginal")
    private boolean isOriginal;

    public void update(final BigDecimal value, final String date, final Boolean isOriginal) {
        if (Objects.nonNull(value)) {
            this.value = value;
        }
        if (Objects.nonNull(date)) {
            this.date = date;
        }
        if (Objects.nonNull(isOriginal)) {
            this.isOriginal = isOriginal;
        }
    }

}
