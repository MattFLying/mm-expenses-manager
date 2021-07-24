package mm.expenses.manager.product.category;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.product.category.command.CreateCategoryCommand.CreateSubcategoryCommand;
import org.apache.commons.collections4.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class SubcategoryTree {

    private final Collection<CreateSubcategoryCommand> subcategoriesCommand;

    @Getter
    private final Collection<Category> allCategories = new ArrayList<>();

    @Getter
    private final Collection<Category> subcategories = new ArrayList<>();

    Set<String> getSubcategoriesIds() {
        return subcategories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
    }

    Map<String, Object> sss() {
        return subcategories.stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        x -> ss(x.getSubcategories())
                ));
    }

    private Object ss(final Set<String> subcategories) {
        if(CollectionUtils.isEmpty(subcategories)) {
            return Collections.emptyMap();
        }


    }

    void prepareCategories(final Instant date, final String parentId) {
        subcategoriesCommand.forEach(sub -> {
            final var subcategory = prepareCategory(sub, parentId, date);

            subcategories.add(subcategory);
        });
    }

    private Category prepareCategory(final CreateSubcategoryCommand subcategoryCommand, final String parentId, final Instant date) {
        final var newSubcategory = Category.builder()
                .id(UUID.randomUUID().toString())
                .name(subcategoryCommand.getName())
                .createdAt(date)
                .lastModifiedAt(date)
                .parentId(parentId)
                .build();

        if (CollectionUtils.isNotEmpty(subcategoryCommand.getSubcategories())) {
            final var subcategoryList = subcategoryCommand.getSubcategories()
                    .stream()
                    .map(subcategory -> prepareCategory(subcategory, newSubcategory.getId(), date))
                    .collect(Collectors.toList());

            newSubcategory.setSubcategories(subcategoryList.stream().map(Category::getId).collect(Collectors.toSet()));
        } else {
            newSubcategory.setSubcategories(new HashSet<>());
        }

        allCategories.add(newSubcategory);
        return newSubcategory;
    }

}
