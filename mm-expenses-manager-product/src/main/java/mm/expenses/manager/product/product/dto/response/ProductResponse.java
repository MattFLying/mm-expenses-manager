package mm.expenses.manager.product.product.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode
@Builder(toBuilder = true)
@JsonPropertyOrder({"id", "name", "price", "details"})
@Schema(name = "ProductResponse", description = "Product response data.")
public class ProductResponse {

    @Schema(description = "Product id.")
    private final String id;

    @Schema(description = "Product name.")
    private final String name;

    @Schema(description = "Product price.")
    private final PriceResponse price;

    @Schema(description = "Product details.")
    private Map<String, Object> details;

}
