package mm.expenses.manager.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Custom product validator for any requests.
 */
class ProductValidator implements ConstraintValidator<ValidateProduct, Object> {

    @Override
    public boolean isValid(Object request, ConstraintValidatorContext context) {
        return true;
    }

}
