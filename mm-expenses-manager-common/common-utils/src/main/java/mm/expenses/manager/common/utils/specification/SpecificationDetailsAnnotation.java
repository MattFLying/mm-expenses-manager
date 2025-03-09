package mm.expenses.manager.common.utils.specification;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used in entities that are exposed to be filtered out or sorted by specific properties
 * related directly with database specification.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpecificationDetailsAnnotation {

    /**
     * @return if expected field can be filtered out, by default is false
     */
    boolean canBeFiltered() default false;

    /**
     * @return if expected field can be sorted, by default is false
     */
    boolean canBeSorted() default false;

    /**
     * @return if expected field is an JSONB representation
     */
    boolean isJsonB() default false;

}
