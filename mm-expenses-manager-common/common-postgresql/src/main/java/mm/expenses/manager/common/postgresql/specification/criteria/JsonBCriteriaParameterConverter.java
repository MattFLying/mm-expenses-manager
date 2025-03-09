package mm.expenses.manager.common.postgresql.specification.criteria;

import jakarta.persistence.criteria.*;
import lombok.val;
import mm.expenses.manager.common.postgresql.specification.FieldType;
import mm.expenses.manager.common.postgresql.specification.Operation;

import static mm.expenses.manager.common.postgresql.specification.criteria.CriteriaParameter.CriteriaParameterBuilder.parseToBoolean;

/**
 * Represents specific converter for specific {@link Operation} for JsonB field types.
 */
public class JsonBCriteriaParameterConverter {

    private static final String ANY_VALUE_SYMBOL = "%";

    public static final String JSONB_EXTRACT_PATH_TEXT_FUNCTION = "jsonb_extract_path_text";
    public static final String JSONB_ARRAY_LENGTH_FUNCTION = "jsonb_array_length";

    /**
     * @return {@link Predicate} created specifically for JsonB field and distinguished by the field type.
     */
    public Predicate convertToPredicateBasedOnFieldType(final CriteriaBuilder builder, final From<?, ?> fromTable, final String rootKey, final String keyToExtract, final CriteriaParameter criteriaParameter) {
        val fieldType = criteriaParameter.fieldType();
        val isJsonBField = criteriaParameter.isJsonBField();
        if (isJsonBField) {
            // if criteria parameter is of JsonB type it should be threatened same way for JsonB and Object field type
            if (fieldType.isOfType(FieldType.JsonB, FieldType.Object)) {
                return createJsonBPathPredicate(builder, fromTable, rootKey, keyToExtract, criteriaParameter);
            }

            // if criteria parameter is of JsonB type it has different handling in case of List field type
            if (fieldType.isOfType(FieldType.List)) {
                return createJsonBArrayPredicate(builder, fromTable, rootKey, keyToExtract, criteriaParameter);
            }
        }
        return null;
    }

    /**
     * @return {@link Predicate} for specific path in JsonB structure.
     */
    private Predicate createJsonBPathPredicate(final CriteriaBuilder builder, final From<?, ?> fromTable, final String rootKey, final String keyToExtract, final CriteriaParameter criteriaParameter) {
        return switch (criteriaParameter.operation()) {
            case isNull -> parseToBoolean(String.valueOf(criteriaParameter.values().get(0)))
                    ? builder.isNull(fromTable.get(rootKey))
                    : builder.isNotNull(fromTable.get(rootKey));
            case equal -> builder.equal(
                    builder.function(JSONB_EXTRACT_PATH_TEXT_FUNCTION, String.class, fromTable.get(rootKey), builder.literal(keyToExtract)),
                    valueAsString(criteriaParameter.values().get(0))
            );
            case notEqual -> builder.notEqual(
                    builder.function(JSONB_EXTRACT_PATH_TEXT_FUNCTION, String.class, fromTable.get(rootKey), builder.literal(keyToExtract)),
                    valueAsString(criteriaParameter.values().get(0))
            );
            case greaterThan -> builder.greaterThan(
                    builder.function(JSONB_EXTRACT_PATH_TEXT_FUNCTION, String.class, fromTable.get(rootKey), builder.literal(keyToExtract)),
                    valueAsString(criteriaParameter.values().get(0))
            );
            case greaterThanOrEqual -> builder.greaterThanOrEqualTo(
                    builder.function(JSONB_EXTRACT_PATH_TEXT_FUNCTION, String.class, fromTable.get(rootKey), builder.literal(keyToExtract)),
                    valueAsString(criteriaParameter.values().get(0))
            );
            case lessThan -> builder.lessThan(
                    builder.function(JSONB_EXTRACT_PATH_TEXT_FUNCTION, String.class, fromTable.get(rootKey), builder.literal(keyToExtract)),
                    valueAsString(criteriaParameter.values().get(0))
            );
            case lessThanOrEqual -> builder.lessThanOrEqualTo(
                    builder.function(JSONB_EXTRACT_PATH_TEXT_FUNCTION, String.class, fromTable.get(rootKey), builder.literal(keyToExtract)),
                    valueAsString(criteriaParameter.values().get(0))
            );
            case count -> builder.equal(
                    builder.function(JSONB_EXTRACT_PATH_TEXT_FUNCTION, String.class, fromTable.get(rootKey), builder.literal(keyToExtract)),
                    criteriaParameter.values()
            );
            case startsWith -> builder.like(
                    builder.function(JSONB_EXTRACT_PATH_TEXT_FUNCTION, String.class, fromTable.get(rootKey), builder.literal(keyToExtract)),
                    String.format("%s%s", valueAsString(criteriaParameter.values().get(0)), ANY_VALUE_SYMBOL)
            );
            case endsWith -> builder.like(
                    builder.function(JSONB_EXTRACT_PATH_TEXT_FUNCTION, String.class, fromTable.get(rootKey), builder.literal(keyToExtract)),
                    String.format("%s%s", ANY_VALUE_SYMBOL, valueAsString(criteriaParameter.values().get(0)))
            );
            case contains -> builder.like(
                    builder.function(JSONB_EXTRACT_PATH_TEXT_FUNCTION, String.class, fromTable.get(rootKey), builder.literal(keyToExtract)),
                    String.format("%s%s%s", ANY_VALUE_SYMBOL, valueAsString(criteriaParameter.values().get(0)), ANY_VALUE_SYMBOL)
            );
            default -> throw new UnsupportedOperationException(keyToExtract);
        };
    }

    /**
     * @return {@link Predicate} for list created as JsonB type.
     */
    private Predicate createJsonBArrayPredicate(final CriteriaBuilder builder, final From<?, ?> fromTable, final String rootKey, final String keyToExtract, final CriteriaParameter criteriaParameter) {
        return switch (criteriaParameter.operation()) {
            case equal -> builder.equal(
                    builder.function(JSONB_ARRAY_LENGTH_FUNCTION, String.class, fromTable.get(rootKey)),
                    criteriaParameter.values().get(0)
            );
            case notEqual -> builder.notEqual(
                    builder.function(JSONB_ARRAY_LENGTH_FUNCTION, String.class, fromTable.get(rootKey)),
                    criteriaParameter.values().get(0)
            );
            case lessThan -> builder.lessThan(
                    arrayLengthFunctionExpression(fromTable, builder, rootKey),
                    valueAsInteger(criteriaParameter.values().get(0))
            );
            case lessThanOrEqual -> builder.lessThanOrEqualTo(
                    arrayLengthFunctionExpression(fromTable, builder, rootKey),
                    valueAsInteger(criteriaParameter.values().get(0))
            );
            case greaterThan -> builder.greaterThan(
                    arrayLengthFunctionExpression(fromTable, builder, rootKey),
                    valueAsInteger(criteriaParameter.values().get(0))
            );
            case greaterThanOrEqual -> builder.greaterThanOrEqualTo(
                    arrayLengthFunctionExpression(fromTable, builder, rootKey),
                    valueAsInteger(criteriaParameter.values().get(0))
            );
            case count -> builder.equal(
                    builder.function(JSONB_ARRAY_LENGTH_FUNCTION, Integer.class, fromTable.get(rootKey)),
                    criteriaParameter.values().get(0)
            );
            case contains -> builder.like(
                    builder.function(JSONB_ARRAY_LENGTH_FUNCTION, String.class, fromTable.get(rootKey), builder.literal(keyToExtract)),
                    String.format("%s%s%s", ANY_VALUE_SYMBOL, valueAsString(criteriaParameter.values().get(0)), ANY_VALUE_SYMBOL)
            );
            default -> throw new UnsupportedOperationException(keyToExtract);
        };
    }

    private Integer valueAsInteger(final Object value) {
        return Integer.valueOf(String.valueOf(value));
    }

    private String valueAsString(final Object value) {
        return String.valueOf(value);
    }

    private Expression<Integer> arrayLengthFunctionExpression(final From<?, ?> fromTable, final CriteriaBuilder builder, final String parameterName) {
        return builder.function(JSONB_ARRAY_LENGTH_FUNCTION, Integer.class, fromTable.get(parameterName)).as(Integer.class);
    }

}
