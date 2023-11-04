package mm.expenses.manager.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrencyValidator.class)
public @interface ValidateCurrency {

    String message() default "Currency is incorrect.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

