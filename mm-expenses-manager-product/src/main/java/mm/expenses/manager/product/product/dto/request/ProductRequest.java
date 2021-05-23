package mm.expenses.manager.product.product.dto.request;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import mm.expenses.manager.product.product.validator.ValidatePrice;
import mm.expenses.manager.product.product.validator.ValidateProduct;

import java.util.Map;

@Data
@ValidateProduct
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
@JsonPropertyOrder({"name", "price", "details"})
@Schema(name = "ProductRequest", description = "Product request data.")
public class ProductRequest {

    @Schema(description = "The name of the product.")
    private String name;

    @ValidatePrice
    @Schema(description = "The price of the product with currency.")
    private PriceRequest price;

    @Schema(description = "Additional data of the product.")
    private Map<String, Object> details;

}
