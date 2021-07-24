package mm.expenses.manager.product.category;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.product.category.command.CreateCategoryCommand;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static mm.expenses.manager.product.category.CategoryContext.saveCategories;
import static mm.expenses.manager.product.category.CategoryContext.saveCategory;

@Data
@EqualsAndHashCode
@Builder(toBuilder = true)
@Document(collection = "categories")
@CompoundIndexes({
        @CompoundIndex(name = "name_idx", def = "{'name': 1}")
})
public class Category {

    @Id
    private final String id;

    private String name;

    private Instant createdAt;

    private Instant lastModifiedAt;

    private String parentId;

    private Set<String> subcategories;

    private Map<String, Object> relatedSubcategories;

    @Version
    private final Long version;

    public void addSubcategory(final String subcategoryID) {
        if (CollectionUtils.isEmpty(subcategories)) {
            this.subcategories = new HashSet<>();
        }
        subcategories.add(subcategoryID);
    }

    public static Category create(final CreateCategoryCommand createCategoryCommand) {
        final var now = DateUtils.now();
        final var newCategory = Category.builder()
                .id(UUID.randomUUID().toString())
                .name(createCategoryCommand.getName())
                .createdAt(now)
                .lastModifiedAt(now)
                .build();

        if (createCategoryCommand.isParent()) {
            newCategory.setParentId(newCategory.getId());
        } else {
            newCategory.setParentId(createCategoryCommand.getParentId());
        }

        if (CollectionUtils.isNotEmpty(createCategoryCommand.getSubcategories())) {
            final var tree = new SubcategoryTree(createCategoryCommand.getSubcategories());
            tree.prepareCategories(now, newCategory.getId());
            newCategory.setSubcategories(tree.getSubcategoriesIds());

            saveCategories(tree.getAllCategories());
            newCategory.setRelatedSubcategories(

            );
        } else {
            newCategory.setSubcategories(Collections.emptySet());
            newCategory.setRelatedSubcategories(Collections.emptyMap());
        }

        return saveCategory(newCategory);
    }

    public boolean isParent() {
        return id.equals(parentId);
    }

}
