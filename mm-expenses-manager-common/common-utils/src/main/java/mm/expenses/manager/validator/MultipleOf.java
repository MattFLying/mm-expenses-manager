package mm.expenses.manager.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
@Repeatable(MultipleOf.List.class)
@Constraint(validatedBy = MultipleOfValidator.class)
public @interface MultipleOf {

    double value() default 0.01;

    String message() default "Incorrect number of digits";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({METHOD, FIELD})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        MultipleOf[] value();
    }

}
