package mm.expenses.manager.product.category;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.product.category.command.CreateCategoryCommand;
import mm.expenses.manager.product.category.command.CreateCategoryCommand.CreateSubcategoryCommand;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CategoryTree {

    private final Category category;

    @Getter
    private final Collection<Category> subcategories = new ArrayList<>();

    @Getter
    private final Map<String, Category> allCategoriesById = new HashMap<>();

    @Getter
    private final Map<String, Object> relatedCategoryIds = new HashMap<>();

    void prepareRelatedCategories() {
        subcategories.forEach(x -> {
            final var relatedCategories = prepare(x.getSubcategories());

            relatedCategoryIds.put(x.getId(), relatedCategories);
        });
    }

    private Map<String, Object> prepare(final Set<String> subcategories) {
        subcategories.stream()
                .map(x -> {
                    final var cat = allCategoriesById.get(x);
                    final var catSub = cat.getSubcategories();

                    if(catSub.isEmpty()) {
                        return Map.of(x, null);
                    } else {
                        final var map = catSub.stream()
                                .collect(Collectors.toMap(
                                        o -> o,
                                        o -> prepare2(o)
                                ));


                        return Map.of(x, map);
                        //return prepare(catSub);
                    }
                })






        return null;
    }

    private Map<String, Object> prepare2(final String subcategory) {
        final var cat = allCategoriesById.get(subcategory);
        final var catSub = cat.getSubcategories();

        if(catSub.isEmpty()) {
            return Map.of(subcategory, null);
        }

        final var ss = catSub.stream()
                .map(x -> allCategoriesById.get(subcategory))
                .collect(Collectors.toMap(
                        o -> o.getId(),
                        o -> o.getSubcategories()
                ));

        return Map.of(subcategory, ss);
    }




    void prepareSubcategories(final CreateCategoryCommand createCategoryCommand) {
        createCategoryCommand.getSubcategories().forEach(sub -> {
            final var subcategory = prepareSubcategory(sub, category.getId());

            subcategories.add(subcategory);
        });
    }

    private Category prepareSubcategory(final CreateSubcategoryCommand subcategoryCommand, final String parentId) {
        final var newSubcategory = Category.builder()
                .id(UUID.randomUUID().toString())
                .name(subcategoryCommand.getName())
                .createdAt(category.getCreatedAt())
                .lastModifiedAt(category.getCreatedAt())
                .parentId(parentId)
                .build();

        if (CollectionUtils.isNotEmpty(subcategoryCommand.getSubcategories())) {
            final var subcategoryList = subcategoryCommand.getSubcategories()
                    .stream()
                    .map(subcategory -> prepareSubcategory(subcategory, newSubcategory.getId()))
                    .collect(Collectors.toList());

            newSubcategory.setSubcategories(subcategoryList.stream().map(Category::getId).collect(Collectors.toSet()));
        } else {
            newSubcategory.setSubcategories(new HashSet<>());
        }

        allCategoriesById.put(newSubcategory.getId(), newSubcategory);
        return newSubcategory;
    }

}
