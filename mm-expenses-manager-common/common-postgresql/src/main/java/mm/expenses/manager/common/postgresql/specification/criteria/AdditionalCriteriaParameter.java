package mm.expenses.manager.common.postgresql.specification.criteria;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import mm.expenses.manager.common.postgresql.specification.FieldType;
import mm.expenses.manager.common.postgresql.specification.Operation;

import java.util.Objects;

/**
 * Represents additional parameter used in {@link org.springframework.data.jpa.domain.Specification} definitions.
 * Can be used as an additional parameters which is not directly handled by handlers and scanners, but
 * it requires the property based on the name to be available to be used in specifications.
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class AdditionalCriteriaParameter {

    public Operation operation;

    public String name;

    public Object value;

    public boolean isStandard;

    public FieldType type;

    /**
     * @return criteria parameter based on the specific name.
     */
    public static AdditionalCriteriaParameter of(final String name) {
        Objects.requireNonNull(name, "Param name cannot be null");

        return build(name, null, null, null, true);
    }

    /**
     * @return criteria parameter based on the specific name and value.
     */
    public static AdditionalCriteriaParameter of(final String name, final Object value) {
        Objects.requireNonNull(name, "Param name cannot be null");
        Objects.requireNonNull(value, "Param value cannot be null");

        return build(name, value, null, null, true);
    }

    /**
     * @return criteria parameter based on the specific name, value and {@link Operation}.
     */
    public static AdditionalCriteriaParameter of(final String name, final Object value, final Operation operation) {
        Objects.requireNonNull(name, "Param name cannot be null");
        Objects.requireNonNull(value, "Param value cannot be null");
        Objects.requireNonNull(operation, "Param operation cannot be null");

        return build(name, value, operation, null, true);
    }

    /**
     * @return criteria parameter based on the specific name, value, {@link Operation}, {@link FieldType} and if it is standard operation's behaviour.
     */
    public static AdditionalCriteriaParameter of(final String name, final Object value, final Operation operation, final FieldType type) {
        Objects.requireNonNull(name, "Param name cannot be null");
        Objects.requireNonNull(value, "Param value cannot be null");
        Objects.requireNonNull(operation, "Param operation cannot be null");
        Objects.requireNonNull(type, "Param type cannot be null");

        return build(name, value, operation, type, true);
    }

    /**
     * @return criteria parameter based on the specific name, value, {@link Operation}, {@link FieldType} and if it is standard operation's behaviour.
     */
    public static AdditionalCriteriaParameter of(final String name, final Object value, final Operation operation, final FieldType type, final boolean isStandard) {
        Objects.requireNonNull(name, "Param name cannot be null");
        Objects.requireNonNull(value, "Param value cannot be null");
        Objects.requireNonNull(operation, "Param operation cannot be null");
        Objects.requireNonNull(type, "Param type cannot be null");

        return build(name, value, operation, type, isStandard);
    }

    /**
     * @return return default {@link AdditionalCriteriaParameter} built object without validation at this level.
     */
    private static AdditionalCriteriaParameter build(final String name, final Object value, final Operation operation, final FieldType type, final boolean isStandard) {
        return AdditionalCriteriaParameter.builder()
                .name(name)
                .value(value)
                .operation(operation)
                .type(type)
                .isStandard(isStandard)
                .build();
    }

}
