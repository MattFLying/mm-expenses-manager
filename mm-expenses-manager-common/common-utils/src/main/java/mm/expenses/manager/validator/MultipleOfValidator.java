package mm.expenses.manager.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Custom validator for any requests related with Price to validate if price value has expected number of digits.
 */
class MultipleOfValidator implements ConstraintValidator<MultipleOf, BigDecimal> {

    private double value;

    @Override
    public void initialize(final MultipleOf constraintAnnotation) {
        this.value = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(final BigDecimal number, final ConstraintValidatorContext context) {
        if (Objects.isNull(number)) {
            return true;
        }
        return (number.doubleValue() / this.value) % 1 == 0;
    }

}
