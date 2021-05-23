package mm.expenses.manager.product.product.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode
@Builder(toBuilder = true)
@JsonPropertyOrder({"value", "currency"})
@Schema(name = "PriceResponse", description = "Price response data.")
public class PriceResponse {

    @Schema(description = "Currency code of the price.")
    private String currency;

    @Schema(description = "Price value.")
    private BigDecimal value;

}
