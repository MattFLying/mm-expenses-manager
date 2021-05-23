package mm.expenses.manager.product.product.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PriceValidator.class)
public @interface ValidatePrice {

    String message() default "Price is incorrect.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
