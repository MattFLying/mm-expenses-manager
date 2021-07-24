package mm.expenses.manager.product.category.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

@Data
@EqualsAndHashCode
@Builder(toBuilder = true)
@JsonPropertyOrder({"id", "name", "parentId", "isParent", "subcategories"})
@Schema(name = "CategoryResponse", description = "Category response data.")
public class CategoryResponse {

    @Schema(description = "Category id.")
    private String id;

    @Schema(description = "Category name.")
    private String name;

    @Schema(description = "Parent category id.")
    private String parentId;

    @Schema(description = "Defines if a category is a parent.")
    private Boolean isParent;

    @Schema(description = "Subcategories of this category.")
    private Collection<String> subcategories;

}
