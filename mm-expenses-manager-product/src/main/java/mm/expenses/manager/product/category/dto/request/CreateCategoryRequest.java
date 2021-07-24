package mm.expenses.manager.product.category.dto.request;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import mm.expenses.manager.product.category.validator.ValidateCategory;

import java.util.Collection;

@Data
@ValidateCategory
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
@JsonPropertyOrder({"name", "parent", "isParent", "subcategories"})
@Schema(name = "CreateCategoryRequest", description = "Category request data.")
public class CreateCategoryRequest {

    @Schema(description = "Category name.")
    private String name;

    @Schema(description = "Parent category id.")
    private String parentId;

    @Schema(description = "Defines if a category is a parent.")
    private Boolean isParent;

    @Schema(description = "Subcategories of this category.")
    private Collection<CreateSubcategoryRequest> subcategories;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Builder(toBuilder = true)
    @JsonPropertyOrder({"name", "subcategories"})
    @Schema(name = "CreateSubcategoryRequest", description = "Subcategory request data.")
    public static class CreateSubcategoryRequest {

        @Schema(description = "Subcategory name.")
        private String name;

        @Schema(description = "Subcategories of this subcategory.")
        private Collection<CreateSubcategoryRequest> subcategories;

    }

}
