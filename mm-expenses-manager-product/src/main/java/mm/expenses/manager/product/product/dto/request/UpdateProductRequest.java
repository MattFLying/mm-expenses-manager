package mm.expenses.manager.product.product.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
@JsonPropertyOrder({"name", "price", "details"})
@Schema(name = "UpdateProductRequest", description = "Product update request data.")
public class UpdateProductRequest {

    @Schema(description = "The name of the product.")
    private String name;

    @Schema(description = "The price of the product with currency.")
    private UpdatePriceRequest price;

    @Schema(description = "Additional data of the product.")
    private Map<String, Object> details;

    @JsonIgnore
    public boolean isAnyUpdate() {
        final var isNameUpdated = Objects.nonNull(name);
        final var isPriceUpdated = Objects.nonNull(price) && price.isAnyUpdate();
        final var isDetailsUpdated = Objects.nonNull(details);

        return isNameUpdated || isPriceUpdated || isDetailsUpdated;
    }

}
