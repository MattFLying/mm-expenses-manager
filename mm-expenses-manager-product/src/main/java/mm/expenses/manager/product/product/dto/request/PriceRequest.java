package mm.expenses.manager.product.product.dto.request;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import mm.expenses.manager.product.product.validator.ValidatePrice;

import java.math.BigDecimal;

@Data
@ValidatePrice
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
@JsonPropertyOrder({"value", "currency"})
@Schema(name = "PriceRequest", description = "Price request data.")
public class PriceRequest {

    @Schema(description = "Currency code of the price.")
    private String currency;

    @Schema(description = "Price value.")
    private BigDecimal value;

}
