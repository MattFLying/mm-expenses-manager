package mm.expenses.manager.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Custom order validator for any requests.
 */
class OrderValidator implements ConstraintValidator<ValidateOrder, Object> {

    @Override
    public boolean isValid(Object request, ConstraintValidatorContext context) {
        return true;
    }

}
