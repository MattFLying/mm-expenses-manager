package mm.expenses.manager.product.product.validator;

import mm.expenses.manager.product.product.dto.request.ProductRequest;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Objects;

/**
 * Custom product validator for any requests.
 */
class ProductValidator implements ConstraintValidator<ValidateProduct, ProductRequest> {

    @Override
    public boolean isValid(final ProductRequest request, final ConstraintValidatorContext context) {
        final var isNameValid = isProductNameValid(request.getName());
        final var isDetailsValid = isProductDetailsValid(request.getDetails());

        context.disableDefaultConstraintViolation();
        if (!isNameValid) {
            context.buildConstraintViolationWithTemplate("The name of the product cannot be empty.").addConstraintViolation();
        }
        if (!isDetailsValid) {
            context.buildConstraintViolationWithTemplate("Details of the product is not correct.").addConstraintViolation();
        }

        return isNameValid && isDetailsValid;
    }

    private boolean isProductNameValid(final String name) {
        return StringUtils.isNotBlank(name);
    }

    private boolean isProductDetailsValid(final Map<String, Object> details) {
        return Objects.nonNull(details);
    }

}
