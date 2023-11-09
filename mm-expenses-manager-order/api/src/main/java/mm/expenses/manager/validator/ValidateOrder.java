package mm.expenses.manager.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderValidator.class)
public @interface ValidateOrder {

    String message() default "Order is incorrect.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

