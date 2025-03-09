package mm.expenses.manager.common.postgresql.exception;

/**
 * Specific exception type to be thrown if there are potential issues with specification criteria.
 */
public class SpecificationCriteriaException extends RuntimeException {

    public static final String INCORRECT_FILTERABLE_FIELD_NAME_MESSAGE = "Incorrect filterable field name: %s";
    public static final String FIELD_NOT_AVAILABLE_IN_OBJECT_FIELD_MESSAGE = "[%s] field have no available [%s] property";
    public static final String OPERATION_IS_UNSUPPORTED_MESSAGE = "Operation: [%s] is unsupported for field: [%s] of type: [%s]";
    public static final String OPERATION_IS_UNAVAILABLE_FOR_BOOLEAN_FIELD_MESSAGE = "Operation: %s is unavailable for boolean field: %s";
    public static final String OPERATION_IS_UNAVAILABLE_FOR_NOT_TEXT_FIELD_MESSAGE = "Operation: %s is unavailable for not text field: %s";
    public static final String OPERATION_REQUIRES_BOOLEAN_VALUE_FOR_FIELD_MESSAGE = "Operation: %s requires boolean value for field: %s";
    public static final String EMPTY_VALUE_FOR_FIELD_OF_TYPE_MESSAGE = "Empty value for field: %s of fieldType: %s";
    public static final String OPERATION_REQUIRES_ONLY_ONE_VALUE_MESSAGE = "Operation: %s requires only one value";
    public static final String EMPTY_LIST_NOT_ALLOWED_FOR_FIELD_MESSAGE = "Empty list not allowed for field: %s";
    public static final String ONLY_ONE_OPERATION_ALLOWED_MESSAGE = "Only one operation allowed";
    public static final String PAGINATION_CONFIG_IS_NULL_MESSAGE = "Pagination configuration is null";
    public static final String BOOLEAN_INVALID_VALUE_MESSAGE = "Allowed values for boolean type are: %s";

    public SpecificationCriteriaException(final String message) {
        super(message);
    }

    public SpecificationCriteriaException(final String message, final Object... params) {
        super(String.format(message, params));
    }

}
