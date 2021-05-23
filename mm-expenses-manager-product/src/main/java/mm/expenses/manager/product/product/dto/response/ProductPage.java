package mm.expenses.manager.product.product.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
@Schema(name = "ProductPage", description = "Paged products response.")
@JsonPropertyOrder({"content", "numberOfElements", "totalElements", "totalPages", "hasNext", "isFirst", "isLast"})
public class ProductPage {

    @NotNull
    private final Collection<ProductResponse> content;

    @Schema(description = "Total available products.")
    private final Long totalElements;

    @Schema(description = "Total available pages of products.")
    private final Integer totalPages;

    @Schema(description = "Defines if there is more available pages.")
    private final Boolean hasNext;

    @Schema(description = "Defines if retrieved page is first.")
    private final Boolean isFirst;

    @Schema(description = "Defines if retrieved page is last.")
    private final Boolean isLast;

    @Schema(description = "All products page.")
    public Collection<ProductResponse> getContent() {
        return content;
    }

    @Schema(description = "Retrieved products count.")
    public Integer getNumberOfElements() {
        return content.size();
    }

}
