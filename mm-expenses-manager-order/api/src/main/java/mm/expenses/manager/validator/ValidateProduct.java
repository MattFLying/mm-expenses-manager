package mm.expenses.manager.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProductValidator.class)
public @interface ValidateProduct {

    String message() default "Product is incorrect.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

