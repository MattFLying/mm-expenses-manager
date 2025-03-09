package mm.expenses.manager.common.postgresql.specification.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import lombok.val;
import mm.expenses.manager.common.postgresql.exception.SpecificationCriteriaException;
import mm.expenses.manager.common.postgresql.specification.FieldType;
import mm.expenses.manager.common.postgresql.specification.Operation;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Represents a simple map of available {@link CriteriaParameterConverter}s for specific {@link Operation}s.
 */
public final class CriteriaParameterConverters extends HashMap<Operation, CriteriaParameterConverter> {

    public static final String JSONB_EXTRACT_PATH_TEXT_FUNCTION = "jsonb_extract_path_text";
    public static final String JSONB_ARRAY_LENGTH_FUNCTION = "jsonb_array_length";

    /**
     * Default constructor that initializes a storage of available converters for available operations.
     */
    public CriteriaParameterConverters() {
        val converters = new Converters();

        val availableOperations = converters.getAvailableConverters();
        val operations = availableOperations.stream()
                .map(Pair::getKey)
                .distinct()
                .toList();

        if (availableOperations.size() != operations.size()) {
            throw new SpecificationCriteriaException("Some converters are duplicated.");
        }
        converters.getAvailableConverters()
                .forEach(converter -> put(converter.getKey(), converter.getValue()));
    }

    /**
     * Returns specific converter for passed {@link CriteriaParameter}.
     */
    public CriteriaParameterConverter getConverter(final CriteriaParameter param) {
        return get(param.operation());
    }

    /**
     * Return all available operations that have custom converters to JPA {@link org.springframework.data.jpa.domain.Specification}.
     */
    public Set<Operation> getAvailableOperations() {
        return keySet();
    }

    /**
     * Contains all available converters for {@link Operation}s that define specific way to convert specific operation
     * to {@link CriteriaParameterConverter} that will be used to define final JPA {@link org.springframework.data.jpa.domain.Specification}.
     */
    private static class Converters {

        private static final String ANY_VALUE_SYMBOL = "%";

        /**
         * Returns values of passed {@link CriteriaParameter}.
         */
        private List<Object> getValues(final CriteriaParameter criteriaParameter) {
            return criteriaParameter.values();
        }

        /**
         * Returns the field name of used {@link CriteriaParameter}.
         */
        private String getParameterName(final CriteriaParameter criteriaParameter) {
            return criteriaParameter.name();
        }

        /**
         * Returns the field type of used {@link CriteriaParameter}.
         */
        private Class<?> getParameterType(final CriteriaParameter criteriaParameter) {
            return criteriaParameter.fieldType().getType();
        }

        /**
         * Defines all available converters for possible operations.
         */
        public List<Pair<Operation, CriteriaParameterConverter>> getAvailableConverters() {
            return List.of(
                    isNullConverter(),
                    equalConverter(), notEqualConverter(),
                    lessThanConverter(), lessThanOrEqualConverter(),
                    greaterThanConverter(), greaterThanOrEqualConverter(),
                    startsWithConverter(), endsWithConverter(),
                    containsConverter(), countConverter()
            );
        }

        private Pair<Operation, CriteriaParameterConverter> countConverter() {
            return Pair.of(
                    Operation.count,
                    (param, root, builder) -> {
                        val paramName = getParameterName(param);
                        val value = getValues(param);
                        val type = getParameterType(param);

                        if (FieldType.List.getSimpleName().equals(type.getSimpleName()) && param.isJsonBField()) {
                            return builder.equal(
                                    builder.function(JSONB_ARRAY_LENGTH_FUNCTION, Integer.class, root.get(paramName)),
                                    value.get(0)
                            );
                        }
                        return builder.equal(
                                builder.function(JSONB_EXTRACT_PATH_TEXT_FUNCTION, String.class, root.get(paramName), builder.literal(paramName)),
                                value
                        );
                    }
            );
        }

        private Pair<Operation, CriteriaParameterConverter> equalConverter() {
            return Pair.of(
                    Operation.equal,
                    (param, root, builder) -> {
                        val paramName = getParameterName(param);
                        val values = getValues(param);
                        val type = getParameterType(param);

                        if (FieldType.List.getSimpleName().equals(type.getSimpleName()) && param.isJsonBField()) {
                            return builder.equal(
                                    builder.function(JSONB_ARRAY_LENGTH_FUNCTION, Integer.class, root.get(paramName)),
                                    values.get(0)
                            );
                        }
                        return values.size() == 1
                                ? builder.equal(root.get(paramName), values.get(0))
                                : root.get(paramName).in(values);
                    }
            );
        }

        private Pair<Operation, CriteriaParameterConverter> notEqualConverter() {
            return Pair.of(
                    Operation.notEqual,
                    (param, root, builder) -> {
                        val paramName = getParameterName(param);
                        val values = getValues(param);
                        val type = getParameterType(param);

                        if (FieldType.List.getSimpleName().equals(type.getSimpleName()) && param.isJsonBField()) {
                            return builder.notEqual(
                                    builder.function(JSONB_ARRAY_LENGTH_FUNCTION, Integer.class, root.get(paramName)),
                                    values.get(0)
                            );
                        }

                        return values.size() == 1
                                ? builder.notEqual(root.get(paramName), values.get(0))
                                : builder.not(root.get(paramName).in(values));
                    }
            );
        }

        private Pair<Operation, CriteriaParameterConverter> lessThanConverter() {
            return Pair.of(
                    Operation.lessThan,
                    (param, root, builder) -> {
                        val paramName = getParameterName(param);
                        val values = getValues(param);
                        val type = getParameterType(param);
                        val isCollection = FieldType.List.getSimpleName().equals(type.getSimpleName());

                        val value = values.get(0);
                        if (value instanceof String textValue && !isCollection) {
                            return builder.lessThan(root.get(paramName), textValue);
                        }
                        if (value instanceof Integer intValue && !isCollection) {
                            return builder.lessThan(root.get(paramName), intValue);
                        }
                        if (value instanceof Long longValue && !isCollection) {
                            return builder.lessThan(root.get(paramName), longValue);
                        }
                        if (value instanceof Instant instantValue && !isCollection) {
                            return builder.lessThan(root.get(paramName), instantValue);
                        }
                        if (value instanceof Boolean boolValue && !isCollection) {
                            return builder.lessThan(root.get(paramName), boolValue);
                        }
                        if (isCollection && param.isJsonBField()) {
                            return builder.lessThan(
                                    arrayLengthFunctionExpression(root, builder, paramName),
                                    valueAsInteger(value)
                            );
                        }
                        throw new SpecificationCriteriaException(SpecificationCriteriaException.OPERATION_IS_UNSUPPORTED_MESSAGE, Operation.lessThan, paramName, type);
                    }
            );
        }

        private Pair<Operation, CriteriaParameterConverter> lessThanOrEqualConverter() {
            return Pair.of(
                    Operation.lessThanOrEqual,
                    (param, root, builder) -> {
                        val paramName = getParameterName(param);
                        val values = getValues(param);
                        val type = getParameterType(param);
                        val isCollection = FieldType.List.getSimpleName().equals(type.getSimpleName());

                        val value = values.get(0);
                        if (value instanceof String textValue && !isCollection) {
                            return builder.lessThanOrEqualTo(root.get(paramName), textValue);
                        }
                        if (value instanceof Integer intValue && !isCollection) {
                            return builder.lessThanOrEqualTo(root.get(paramName), intValue);
                        }
                        if (value instanceof Long longValue && !isCollection) {
                            return builder.lessThanOrEqualTo(root.get(paramName), longValue);
                        }
                        if (value instanceof Instant instantValue && !isCollection) {
                            return builder.lessThanOrEqualTo(root.get(paramName), instantValue);
                        }
                        if (value instanceof Boolean boolValue && !isCollection) {
                            return builder.lessThanOrEqualTo(root.get(paramName), boolValue);
                        }
                        if (isCollection && param.isJsonBField()) {
                            return builder.lessThanOrEqualTo(
                                    arrayLengthFunctionExpression(root, builder, paramName),
                                    valueAsInteger(value)
                            );
                        }
                        throw new SpecificationCriteriaException(SpecificationCriteriaException.OPERATION_IS_UNSUPPORTED_MESSAGE, Operation.lessThanOrEqual, paramName, type);
                    }
            );
        }

        private Pair<Operation, CriteriaParameterConverter> greaterThanConverter() {
            return Pair.of(
                    Operation.greaterThan,
                    (param, root, builder) -> {
                        val paramName = getParameterName(param);
                        val values = getValues(param);
                        val type = getParameterType(param);
                        val isCollection = FieldType.List.getSimpleName().equals(type.getSimpleName());

                        val value = values.get(0);
                        if (value instanceof String textValue && !isCollection) {
                            return builder.greaterThan(root.get(paramName), textValue);
                        }
                        if (value instanceof Integer intValue && !isCollection) {
                            return builder.greaterThan(root.get(paramName), intValue);
                        }
                        if (value instanceof Long longValue && !isCollection) {
                            return builder.greaterThan(root.get(paramName), longValue);
                        }
                        if (value instanceof Instant instantValue && !isCollection) {
                            return builder.greaterThan(root.get(paramName), instantValue);
                        }
                        if (value instanceof Boolean boolValue && !isCollection) {
                            return builder.greaterThan(root.get(paramName), boolValue);
                        }
                        if (isCollection && param.isJsonBField()) {
                            return builder.greaterThan(
                                    arrayLengthFunctionExpression(root, builder, paramName),
                                    valueAsInteger(value)
                            );
                        }
                        throw new SpecificationCriteriaException(SpecificationCriteriaException.OPERATION_IS_UNSUPPORTED_MESSAGE, Operation.greaterThan, paramName, type);
                    }
            );
        }

        private Pair<Operation, CriteriaParameterConverter> greaterThanOrEqualConverter() {
            return Pair.of(
                    Operation.greaterThanOrEqual,
                    (param, root, builder) -> {
                        val paramName = getParameterName(param);
                        val values = getValues(param);
                        val type = getParameterType(param);
                        val isCollection = FieldType.List.getSimpleName().equals(type.getSimpleName());

                        val value = values.get(0);
                        if (value instanceof String textValue && !isCollection) {
                            return builder.greaterThanOrEqualTo(root.get(paramName), textValue);
                        }
                        if (value instanceof Integer intValue && !isCollection) {
                            return builder.greaterThanOrEqualTo(root.get(paramName), intValue);
                        }
                        if (value instanceof Long longValue && !isCollection) {
                            return builder.greaterThanOrEqualTo(root.get(paramName), longValue);
                        }
                        if (value instanceof Instant instantValue && !isCollection) {
                            return builder.greaterThanOrEqualTo(root.get(paramName), instantValue);
                        }
                        if (value instanceof Boolean boolValue && !isCollection) {
                            return builder.greaterThanOrEqualTo(root.get(paramName), boolValue);
                        }
                        if (isCollection && param.isJsonBField()) {
                            return builder.greaterThanOrEqualTo(
                                    arrayLengthFunctionExpression(root, builder, paramName),
                                    valueAsInteger(value)
                            );
                        }
                        throw new SpecificationCriteriaException(SpecificationCriteriaException.OPERATION_IS_UNSUPPORTED_MESSAGE, Operation.greaterThanOrEqual, paramName, type);
                    }
            );
        }

        private Pair<Operation, CriteriaParameterConverter> isNullConverter() {
            return Pair.of(
                    Operation.isNull,
                    (param, root, builder) -> {
                        val paramName = getParameterName(param);
                        val type = getParameterType(param);
                        val values = getValues(param);
                        val value = values.get(0);
                        if (value instanceof Boolean boolValue) {
                            return (boolean) boolValue ? builder.isNull(root.get(paramName)) : builder.isNotNull(root.get(paramName));
                        }
                        throw new SpecificationCriteriaException(SpecificationCriteriaException.OPERATION_IS_UNSUPPORTED_MESSAGE, Operation.isNull, paramName, type);
                    }
            );
        }

        private Pair<Operation, CriteriaParameterConverter> startsWithConverter() {
            return Pair.of(
                    Operation.startsWith,
                    (param, root, builder) -> {
                        val paramName = getParameterName(param);
                        val type = getParameterType(param);
                        val values = getValues(param);
                        val value = values.get(0);
                        if (value instanceof String textValue) {
                            return builder.like(root.get(paramName), String.format("%s%s", textValue, ANY_VALUE_SYMBOL));
                        }
                        throw new SpecificationCriteriaException(SpecificationCriteriaException.OPERATION_IS_UNSUPPORTED_MESSAGE, Operation.startsWith, paramName, type);
                    }
            );
        }

        private Pair<Operation, CriteriaParameterConverter> endsWithConverter() {
            return Pair.of(
                    Operation.endsWith,
                    (param, root, builder) -> {
                        val paramName = getParameterName(param);
                        val type = getParameterType(param);
                        val values = getValues(param);
                        val value = values.get(0);
                        if (value instanceof String textValue) {
                            return builder.like(root.get(paramName), String.format("%s%s", ANY_VALUE_SYMBOL, textValue));
                        }
                        throw new SpecificationCriteriaException(SpecificationCriteriaException.OPERATION_IS_UNSUPPORTED_MESSAGE, Operation.endsWith, paramName, type);
                    }
            );
        }

        private Pair<Operation, CriteriaParameterConverter> containsConverter() {
            return Pair.of(
                    Operation.contains,
                    (param, root, builder) -> {
                        val paramName = getParameterName(param);
                        val type = getParameterType(param);
                        val values = getValues(param);
                        val value = values.get(0);
                        if (value instanceof String textValue) {
                            return builder.like(root.get(paramName), String.format("%s%s%s", ANY_VALUE_SYMBOL, textValue, ANY_VALUE_SYMBOL));
                        }
                        throw new SpecificationCriteriaException(SpecificationCriteriaException.OPERATION_IS_UNSUPPORTED_MESSAGE, Operation.contains, paramName, type);
                    }
            );
        }

        private Integer valueAsInteger(final Object value) {
            return Integer.valueOf(String.valueOf(value));
        }

        private Expression<Integer> arrayLengthFunctionExpression(final Root<?> root, final CriteriaBuilder builder, final String parameterName) {
            return builder.function(JSONB_ARRAY_LENGTH_FUNCTION, Integer.class, root.get(parameterName)).as(Integer.class);
        }

    }

}
