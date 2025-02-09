package mm.expenses.manager.common.postgresql.specification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.postgresql.exception.OperationException;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents available operations on entity's fields to be evaluated during specification creation.
 */
@Getter
@RequiredArgsConstructor
public enum Operation {
    isNull(Operation.SEPARATOR + "isNull"),

    equal(Operation.SEPARATOR + "equal"),
    notEqual(Operation.SEPARATOR + "notEqual"),

    lessThan(Operation.SEPARATOR + "lessThan"),
    lessThanOrEqual(Operation.SEPARATOR + "lessThanOrEqual"),

    greaterThan(Operation.SEPARATOR + "greaterThan"),
    greaterThanOrEqual(Operation.SEPARATOR + "greaterThanOrEqual"),

    startsWith(Operation.SEPARATOR + "startsWith"),
    endsWith(Operation.SEPARATOR + "endsWith"),
    contains(Operation.SEPARATOR + "contains"),
    count(Operation.SEPARATOR + "count");

    public static final String SEPARATOR = ":";

    private final String valueWithSeparator;

    /**
     * @return true if operation type is present in given operation types, otherwise return false.
     */
    public boolean isIn(final Operation... expectedOperations) {
        Objects.requireNonNull(expectedOperations, "Expected operations cannot be null");
        return Arrays.asList(expectedOperations).contains(this);
    }

    /**
     * @return correct {@link Operation} of given enum value otherwise returns Operation.equal type.
     */
    public static Operation of(final Enum<?> enumValue) {
        if (Objects.nonNull(enumValue)) {
            try {
                if (enumValue instanceof Operation operation) {
                    return valueOf(operation.name());
                }
                return valueOf(enumValue.name());
            } catch (final IllegalArgumentException exception) {
                throw new OperationException(String.format(OperationException.UNRECOGNIZABLE_OPERATION_MESSAGE, enumValue), exception);
            }
        }
        return Operation.equal;
    }

}