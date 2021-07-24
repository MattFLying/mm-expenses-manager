package mm.expenses.manager.product.category.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class CreateCategoryCommand {

    private String name;

    private String parentId;

    private Boolean isParent;

    private Collection<CreateSubcategoryCommand> subcategories;

    public boolean isParent() {
        return Objects.nonNull(isParent) && isParent;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Builder(toBuilder = true)
    public static class CreateSubcategoryCommand {

        private String name;

        private Collection<CreateSubcategoryCommand> subcategories;

    }

}
