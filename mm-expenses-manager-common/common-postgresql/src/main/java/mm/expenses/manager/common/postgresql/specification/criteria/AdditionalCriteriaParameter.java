package mm.expenses.manager.common.postgresql.specification.criteria;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
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

    /**
     * @return criteria parameter based on the specific name.
     */
    public static AdditionalCriteriaParameter of(final String name) {
        Objects.requireNonNull(name, "Param name cannot be null");

        return AdditionalCriteriaParameter.builder()
                .name(name)
                .build();
    }

    /**
     * @return criteria parameter based on the specific name and value.
     */
    public static AdditionalCriteriaParameter of(final String name, final Object value) {
        Objects.requireNonNull(name, "Param name cannot be null");
        Objects.requireNonNull(value, "Param value cannot be null");

        return AdditionalCriteriaParameter.builder()
                .name(name)
                .value(value)
                .build();
    }

    /**
     * @return criteria parameter based on the specific name, value and {@link Operation}.
     */
    public static AdditionalCriteriaParameter of(final String name, final Object value, final Operation operation) {
        Objects.requireNonNull(name, "Param name cannot be null");
        Objects.requireNonNull(value, "Param value cannot be null");
        Objects.requireNonNull(operation, "Param operation cannot be null");

        return AdditionalCriteriaParameter.builder()
                .name(name)
                .value(value)
                .operation(operation)
                .build();
    }

}
