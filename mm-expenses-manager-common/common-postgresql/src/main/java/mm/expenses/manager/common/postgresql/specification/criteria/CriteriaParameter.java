package mm.expenses.manager.common.postgresql.specification.criteria;

import lombok.val;
import mm.expenses.manager.common.postgresql.exception.SpecificationCriteriaException;
import mm.expenses.manager.common.postgresql.specification.FieldType;
import mm.expenses.manager.common.postgresql.specification.Operation;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Representation of specification criteria parameter to be handled by JPA specification in further processing.
 */
public record CriteriaParameter(String name, FieldType fieldType, boolean isJsonBField, Operation operation, List<Object> values) {

    /**
     * Represents specification criteria builder for specific parameter.
     */
    public static class CriteriaParameterBuilder {

        private static final String BOOLEAN_TRUE_TEXT = "true";
        private static final String BOOLEAN_FALSE_TEXT = "false";
        private static final String BOOLEAN_TRUE_NUMBER = "1";
        private static final String BOOLEAN_FALSE_NUMBER = "0";

        private final Map.Entry<String, String> entry;

        private final SpecificationCriteria criteria;

        private Operation operation;

        private String name;

        private String value;

        public CriteriaParameterBuilder(final Map.Entry<String, String> entry, final SpecificationCriteria criteria) {
            this.entry = entry;
            this.criteria = criteria;
        }

        /**
         * Builds specification criteria for specific parameter.
         */
        public CriteriaParameter build() {
            parseMapEntryToCorrectProperties();

            val splitNameBySeparator = name.split("\\.");
            val filteredField = criteria.getFilteredField(splitNameBySeparator[0]);
            if (Objects.isNull(filteredField)) {
                throw new SpecificationCriteriaException(SpecificationCriteriaException.INCORRECT_FILTERABLE_FIELD_NAME_MESSAGE, name);
            }

            if (Objects.equals(filteredField.getType(), FieldType.Boolean) && !Objects.equals(operation, Operation.equal)) {
                throw new SpecificationCriteriaException(SpecificationCriteriaException.OPERATION_IS_UNAVAILABLE_FOR_BOOLEAN_FIELD_MESSAGE, operation, name);
            }

            val isIncorrectOperation = operation.isIn(Operation.startsWith, Operation.endsWith, Operation.contains);
            if (isIncorrectOperation && !FieldType.String.equals(filteredField.getType())) {
                throw new SpecificationCriteriaException(SpecificationCriteriaException.OPERATION_IS_UNAVAILABLE_FOR_NOT_TEXT_FIELD_MESSAGE, operation, name);
            }
            return createCriteriaParameter(filteredField);
        }

        private void parseMapEntryToCorrectProperties() {
            this.name = entry.getKey().trim();
            this.value = entry.getValue();
            this.operation = Operation.equal;

            val splitKey = name.split(Operation.SEPARATOR);
            if (splitKey.length > 2) {
                throw new SpecificationCriteriaException(SpecificationCriteriaException.ONLY_ONE_OPERATION_ALLOWED_MESSAGE);
            }

            if (splitKey.length == 2) {
                operation = Operation.valueOf(splitKey[1]);
                name = splitKey[0];
            }
        }

        private CriteriaParameter createCriteriaParameter(FilteredField field) {
            val isBooleanValueExpected = Operation.isNull.equals(operation);
            if (isBooleanValueExpected && !StringUtils.containsAnyIgnoreCase(value, BOOLEAN_TRUE_TEXT, BOOLEAN_TRUE_NUMBER, BOOLEAN_FALSE_TEXT, BOOLEAN_FALSE_NUMBER)) {
                throw new SpecificationCriteriaException(SpecificationCriteriaException.OPERATION_REQUIRES_BOOLEAN_VALUE_FOR_FIELD_MESSAGE, operation, name);
            }

            val parsedValues = isBooleanValueExpected
                    ? List.<Object>of(parseToBoolean(value))
                    : field.validateValues(value);
            if (parsedValues.isEmpty()) {
                throw new SpecificationCriteriaException(SpecificationCriteriaException.EMPTY_VALUE_FOR_FIELD_OF_TYPE_MESSAGE, name, field.getType());
            }
            if (parsedValues.size() != 1 && !operation.isIn(Operation.equal, Operation.notEqual)) {
                throw new SpecificationCriteriaException(SpecificationCriteriaException.OPERATION_REQUIRES_ONLY_ONE_VALUE_MESSAGE, operation);
            }
            if (FieldType.List.equals(field.getType()) && StringUtils.equals(String.valueOf(parsedValues.get(0)), "[]")) {
                throw new SpecificationCriteriaException(SpecificationCriteriaException.EMPTY_LIST_NOT_ALLOWED_FOR_FIELD_MESSAGE, name);
            }
            return new CriteriaParameter(name, field.getType(), field.getIsJsonBType(), operation, parsedValues);
        }

        private Boolean parseToBoolean(String value) {
            if (StringUtils.equalsAnyIgnoreCase(value, BOOLEAN_TRUE_TEXT, BOOLEAN_TRUE_NUMBER)) {
                return Boolean.TRUE;
            } else if (StringUtils.equalsAnyIgnoreCase(value, BOOLEAN_FALSE_TEXT, BOOLEAN_FALSE_NUMBER)) {
                return Boolean.FALSE;
            }
            throw new SpecificationCriteriaException(SpecificationCriteriaException.BOOLEAN_INVALID_VALUE_MESSAGE, BOOLEAN_TRUE_TEXT, BOOLEAN_TRUE_NUMBER, BOOLEAN_FALSE_TEXT, BOOLEAN_FALSE_NUMBER);
        }

    }

}
