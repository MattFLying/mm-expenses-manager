package mm.expenses.manager.product.category.validator;

import mm.expenses.manager.product.category.dto.request.CreateCategoryRequest;
import mm.expenses.manager.product.category.dto.request.CreateCategoryRequest.CreateSubcategoryRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Objects;

/**
 * Custom product validator for any requests.
 */
public class CategoryValidator implements ConstraintValidator<ValidateCategory, CreateCategoryRequest> {

    @Override
    public boolean isValid(final CreateCategoryRequest request, final ConstraintValidatorContext context) {
        final var isNameValid = isNameValid(request.getName());
        final var isParentValid = isParentValid(request);
        final var areSubcategoriesValid = areSubcategoriesValid(request.getSubcategories());

        context.disableDefaultConstraintViolation();
        if (!isNameValid) {
            context.buildConstraintViolationWithTemplate("The name of the category cannot be empty.").addConstraintViolation();
        }

        if (!isParentValid) {
            context.buildConstraintViolationWithTemplate("The parent of the category cannot be empty. Also category can be marked as parent or assigned some id at once.").addConstraintViolation();
        }

        if (!areSubcategoriesValid) {
            context.buildConstraintViolationWithTemplate("Subcategories of this category are incorrect, some names are wrong.").addConstraintViolation();
        }

        return isNameValid && isParentValid && areSubcategoriesValid;
    }

    public static boolean areSubcategoriesValid(final Collection<CreateSubcategoryRequest> subcategories) {
        if (CollectionUtils.isEmpty(subcategories)) {
            return true;
        }
        return subcategories.stream().allMatch(subcategory -> isNameValid(subcategory.getName()));
    }

    public static boolean isParentValid(final CreateCategoryRequest request) {
        final var parentId = request.getParentId();
        final var isParent = request.getIsParent();

        if (Objects.nonNull(parentId)) {
            if (Objects.nonNull(isParent)) {
                return false;
            }
            return StringUtils.isNotBlank(parentId);
        } else {
            if (Objects.isNull(isParent)) {
                return false;
            }
            return isParent;
        }
    }

    public static boolean isNameValid(final String name) {
        return StringUtils.isNotBlank(name);
    }

}
