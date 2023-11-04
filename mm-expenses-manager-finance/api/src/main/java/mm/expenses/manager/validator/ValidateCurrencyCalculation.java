package mm.expenses.manager.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrencyCalculationValidator.class)
public @interface ValidateCurrencyCalculation {

    String message() default "Currency for conversion is incorrect.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

