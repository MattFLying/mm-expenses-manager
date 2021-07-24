package mm.expenses.manager.product.category;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;

import static mm.expenses.manager.product.repository.RepositoryRegistry.categoryRepository;

/**
 * Product context to process any operation on products.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryContext {

    public static Category saveCategory(final Category category) {
        return categoryRepository().save(category);
    }

    public static Collection<Category> saveCategories(final Collection<Category> categories) {
        return categoryRepository().saveAll(categories);
    }

}
