package mm.expenses.manager.product.product.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
@JsonPropertyOrder({"value", "currency"})
@Schema(name = "UpdatePriceRequest", description = "Price update request data.")
public class UpdatePriceRequest {

    @Schema(description = "Currency code of the price.")
    private String currency;

    @Schema(description = "Price value.")
    private BigDecimal value;

    @JsonIgnore
    public boolean isAnyUpdate() {
        final var isCurrencyUpdated = Objects.nonNull(currency);
        final var isValueUpdated = Objects.nonNull(value);

        return isCurrencyUpdated || isValueUpdated;
    }

}
